package com.example.coreserver.service.countermeasure;

import com.example.coreserver.controller.OperationSseController;
import com.example.coreserver.entity.countermeasure.CountermeasureAction;
import com.example.coreserver.entity.countermeasure.CountermeasureOmcFlag;
import com.example.coreserver.grpc.common.DeviceId;
import com.example.coreserver.grpc.talent.CaptureRequest;
import com.example.coreserver.grpc.talent.ConnectionStatus;
import com.example.coreserver.grpc.talent.DriveAngleRequest;
import com.example.coreserver.grpc.talent.PositionRequest;
import com.example.coreserver.grpc.talent.TransmitPowerRequest;
import com.example.coreserver.service.device.TalentService;
import com.example.coreserver.wsserver.netty.NettyDataHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动处置动作下发与收口执行器。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CountermeasureActionExecutionService {

    private static final double MIN_DISTANCE_KM = 0.1D;
    private static final double DEFAULT_DISTANCE_KM = 1.0D;
    private static final int MIN_TRANSMIT_POWER_DBM = 6;

    private final CountermeasureConfigService countermeasureConfigService;
    private final CountermeasureDeviceDirectoryService countermeasureDeviceDirectoryService;
    private final CountermeasureJammingAdapterRegistry jammingAdapterRegistry;
    private final TalentService talentService;
    private final OperationSseController controller;
    private final NettyDataHolder nettyDataHolder;

    private final Object actionLock = new Object();
    private volatile ActiveExecutionState activeExecutionState;

    public void stopCurrentIntervention(String reason) {
        synchronized (actionLock) {
            controller.sendOperation("自动处置收口触发: " + reason + "，正在收口当前处置动作...");
            stopActiveExecutionLocked(reason);
        }
    }

    public void beforeNewRound() {
        synchronized (actionLock) {
            controller.sendOperation("新一轮自动处置即将开始，正在收口上一轮处置动作...");
            stopActiveExecutionLocked("上一轮到期，准备开始新轮次");
        }
    }

    public void executePlan(
            CountermeasureAction action,
            CountermeasureRoundTargetService.AssessedTarget target,
            String reason
    ) {
        synchronized (actionLock) {
            executePlanLocked(action, target, reason);
        }
    }

    private void executePlanLocked(
            CountermeasureAction action,
            CountermeasureRoundTargetService.AssessedTarget target,
            String reason
    ) {
        controller.sendOperation("开始执行自动处置动作: 对象Id => " + target.id());
        log.info("开始执行自动处置动作: action={}, targetId={}, source={}, threatLevel={}, score={}, reason={}",
                action, target.id(), target.snapshot().dataSource(), target.threatLevel(), target.threatScore(), reason);
        switch (action) {
            case UAV_ATTACK_AUTO -> executeJammingInterferenceLocked(target, reason);
            case DECEPTION_DRIVE -> executeDeceptionDriveLocked(target, reason);
            case DECEPTION_CAPTURE -> executeDeceptionCaptureLocked(target, reason);
            case DECEPTION_DEFENSE -> executeDeceptionDefenseLocked(target, reason);
            case DECEPTION_INTERFERENCE -> executeDeceptionInterferenceLocked(target, reason);
            case DECEPTION_NO_FLY -> executeDeceptionNoFlyLocked(target, reason);
            default -> {
                controller.sendOperation("自动处置动作下发: 识别到无需下发设备指令的处置动作，系统将继续监控目标状态变化。");
                log.info("本轮动作解析结果为{}，无需下发设备指令", action);
            }
        }
    }

    private void executeJammingInterferenceLocked(
            CountermeasureRoundTargetService.AssessedTarget target,
            String reason
    ) {
        CountermeasureOmcFlag omcFlag = countermeasureConfigService.getOmcFlag();
        List<CountermeasureDeviceDirectoryService.CountermeasureRuntimeDevice> jammingDevices =
                countermeasureDeviceDirectoryService.resolveExecutableJammingDevices(omcFlag);
        if (jammingDevices.isEmpty()) {
            controller.sendOperation("自动处置动作跳过下发: 当前OMC策略下没有可执行的干扰设备。");
            log.warn("跳过干扰下发: omcFlag={}, reason=没有可执行干扰设备", omcFlag);
            return;
        }

        List<StartedJammingExecution> startedExecutions = new ArrayList<>();
        List<String> configuredFrequencies = countermeasureConfigService.getJammingFrequencies();
        for (CountermeasureDeviceDirectoryService.CountermeasureRuntimeDevice device : jammingDevices) {
            CountermeasureJammingCommandAdapter adapter = jammingAdapterRegistry.find(device.adapterId()).orElse(null);
            if (adapter == null) {
                log.warn("跳过干扰设备: 未找到适配器, deviceId={}, collectFlag={}, adapterId={}",
                        device.id(), device.collectFlag(), device.adapterId());
                continue;
            }
            CountermeasureJammingPlan plan = adapter.buildPlan(device, configuredFrequencies);
            sendCommands(plan.startCommands());
            startedExecutions.add(new StartedJammingExecution(device, plan.description(), plan.stopCommands()));
            log.info("自动处置干扰指令已下发: targetId={}, deviceId={}, collectFlag={}, description={}, reason={}",
                    target.id(), device.id(), device.collectFlag(), plan.description(), reason);
        }

        if (startedExecutions.isEmpty()) {
            controller.sendOperation("自动处置动作跳过下发: 干扰设备适配器未生成可执行命令。");
            log.warn("跳过干扰下发: omcFlag={}, reason=适配器未生成命令", omcFlag);
            return;
        }

        activeExecutionState = new ActiveExecutionState(CountermeasureAction.UAV_ATTACK_AUTO, target.id(), null, startedExecutions);
        controller.sendOperation("自动处置动作下发: 干扰指令已发送，设备数量=" + startedExecutions.size());
    }

    private void executeDeceptionDriveLocked(
            CountermeasureRoundTargetService.AssessedTarget target,
            String reason
    ) {
        CountermeasureOmcFlag omcFlag = countermeasureConfigService.getOmcFlag();
        if (countermeasureDeviceDirectoryService.resolveExecutableSpoofingDevices(omcFlag).isEmpty()) {
            controller.sendOperation("自动处置动作跳过下发: 当前OMC策略下没有可执行的诱骗设备。");
            log.warn("跳过诱骗驱离: omcFlag={}, reason=没有可执行诱骗设备", omcFlag);
            return;
        }
        String deceptionDeviceId = countermeasureDeviceDirectoryService.resolveDeceptionRuntimeDeviceId();
        if (deceptionDeviceId == null) {
            controller.sendOperation("自动处置动作跳过下发: 未找到诱骗设备，无法执行诱骗驱离动作。");
            log.warn("跳过诱骗驱离: 未找到TALENT设备");
            return;
        }
        if (!ensureDeceptionConnected(deceptionDeviceId)) {
            return;
        }
        int powerDbm = calculatePowerDbm(target.snapshot().range());
        double angle = calculateDriveAngle(target);
        talentService.sendTransmitPowerCommand(TransmitPowerRequest.newBuilder()
                .setDeviceId(deceptionDeviceId)
                .setPower(powerDbm)
                .build());
        talentService.sendDriveAngleCommand(DriveAngleRequest.newBuilder()
                .setDeviceId(deceptionDeviceId)
                .setAngle(angle)
                .build());
        activeExecutionState = new ActiveExecutionState(CountermeasureAction.DECEPTION_DRIVE, target.id(), deceptionDeviceId, List.of());
        controller.sendOperation("自动处置动作下发: 诱骗驱离指令已发送，目标将被诱骗向远离地图中心的方向驱离，功率: " + powerDbm + ", 角度: " + angle);
        log.info("自动处置动作已下发: action=DECEPTION_DRIVE, targetId={}, angle={}, power={}, reason={}",
                target.id(), angle, powerDbm, reason);
    }

    private void executeDeceptionCaptureLocked(
            CountermeasureRoundTargetService.AssessedTarget target,
            String reason
    ) {
        String deceptionDeviceId = resolveExecutableDeceptionDeviceId("诱骗捕获");
        if (deceptionDeviceId == null) {
            return;
        }
        int powerDbm = calculatePowerDbm(target.snapshot().range());
        CountermeasureConfigService.GeoPoint capturePoint = resolveCapturePoint(target);
        int altitude = (int) Math.round(capturePoint.altitude());
        double latitude = capturePoint.latitude();
        double longitude = capturePoint.longitude();

        talentService.sendTransmitPowerCommand(TransmitPowerRequest.newBuilder()
                .setDeviceId(deceptionDeviceId)
                .setPower(powerDbm)
                .build());
        talentService.sendBootstrapPositionCommand(PositionRequest.newBuilder()
                .setDeviceId(deceptionDeviceId)
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setAltitude(altitude)
                .build());
        talentService.sendCaptureCommand(CaptureRequest.newBuilder()
                .setDeviceId(deceptionDeviceId)
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setAltitude(altitude)
                .setSimulationLevel(0)
                .setCaptureType(false)
                .build());
        activeExecutionState = new ActiveExecutionState(CountermeasureAction.DECEPTION_CAPTURE, target.id(), deceptionDeviceId, List.of());
        controller.sendOperation("自动处置动作下发: 诱骗捕获指令已发送，目标将被诱骗至捕获点，等待捕获完成。");
        log.info("自动处置动作已下发: action=DECEPTION_CAPTURE, targetId={}, power={}, capturePoint=[{}, {}, {}], reason={}",
                target.id(), powerDbm, longitude, latitude, altitude, reason);
    }

    private void executeDeceptionDefenseLocked(
            CountermeasureRoundTargetService.AssessedTarget target,
            String reason
    ) {
        String deceptionDeviceId = resolveExecutableDeceptionDeviceId("诱骗防御");
        if (deceptionDeviceId == null) {
            return;
        }
        talentService.sendDefenseCommand(buildDeviceId(deceptionDeviceId));
        activeExecutionState = new ActiveExecutionState(CountermeasureAction.DECEPTION_DEFENSE, target.id(), deceptionDeviceId, List.of());
        controller.sendOperation("自动处置动作下发: 诱骗防御指令已发送，设备将进入安全防御模式。");
        log.info("自动处置动作已下发: action=DECEPTION_DEFENSE, targetId={}, deviceId={}, reason={}",
                target.id(), deceptionDeviceId, reason);
    }

    private void executeDeceptionInterferenceLocked(
            CountermeasureRoundTargetService.AssessedTarget target,
            String reason
    ) {
        String deceptionDeviceId = resolveExecutableDeceptionDeviceId("诱骗导航干扰");
        if (deceptionDeviceId == null) {
            return;
        }
        talentService.sendInterferenceCommand(buildDeviceId(deceptionDeviceId));
        activeExecutionState = new ActiveExecutionState(CountermeasureAction.DECEPTION_INTERFERENCE, target.id(), deceptionDeviceId, List.of());
        controller.sendOperation("自动处置动作下发: 诱骗导航干扰指令已发送。");
        log.info("自动处置动作已下发: action=DECEPTION_INTERFERENCE, targetId={}, deviceId={}, reason={}",
                target.id(), deceptionDeviceId, reason);
    }

    private void executeDeceptionNoFlyLocked(
            CountermeasureRoundTargetService.AssessedTarget target,
            String reason
    ) {
        String deceptionDeviceId = resolveExecutableDeceptionDeviceId("诱骗禁飞迫降");
        if (deceptionDeviceId == null) {
            return;
        }
        talentService.sendNoFly(buildDeviceId(deceptionDeviceId));
        activeExecutionState = new ActiveExecutionState(CountermeasureAction.DECEPTION_NO_FLY, target.id(), deceptionDeviceId, List.of());
        controller.sendOperation("自动处置动作下发: 诱骗禁飞迫降指令已发送。");
        log.info("自动处置动作已下发: action=DECEPTION_NO_FLY, targetId={}, deviceId={}, reason={}",
                target.id(), deceptionDeviceId, reason);
    }

    private void sendCommands(List<CountermeasureNettyCommand> commands) {
        for (CountermeasureNettyCommand command : commands) {
            controller.sendOperation("自动处置干扰设备下发: " + command.deviceCode() + " -> " + command.command());
            log.info("发送自动处置干扰命令: deviceCode={}, command={}, args={}",
                    command.deviceCode(), command.command(), command.args());
            nettyDataHolder.command(command.deviceCode(), command.command(), command.args());
        }
    }

    private double calculateDriveAngle(CountermeasureRoundTargetService.AssessedTarget target) {
        CountermeasureConfigService.GeoPoint centerPoint = countermeasureConfigService.getMapCenterPoint();
        if (centerPoint == null) {
            double fallback = target.snapshot().azimuth() == null ? 0.0D : target.snapshot().azimuth().doubleValue();
            controller.sendOperation("自动处置动作下发警告: 地图中心点缺失，驱离角改为使用目标原始方位角。");
            log.warn("地图中心点缺失，驱离角改为使用目标原始方位角: targetId={}, azimuth={}", target.id(), fallback);
            return fallback;
        }
        double bearingToCenter = calculateBearingDegrees(
                toDouble(target.snapshot().latitude()),
                toDouble(target.snapshot().longitude()),
                centerPoint.latitude(),
                centerPoint.longitude()
        );
        double reverseBearing = normalizeDegrees(bearingToCenter + 180.0D);
        controller.sendOperation("自动处置动作下发警告: 驱离角计算完成，目标将被诱骗向远离地图中心的方向驱离，驱离角=" + reverseBearing + "度。");
        log.info("驱离角计算完成: targetId={}, bearingToCenter={}, reverseBearing={}",
                target.id(), bearingToCenter, reverseBearing);
        return reverseBearing;
    }

    private CountermeasureConfigService.GeoPoint resolveCapturePoint(CountermeasureRoundTargetService.AssessedTarget target) {
        CountermeasureConfigService.GeoPoint capturePoint = countermeasureConfigService.getCapturePoint();
        if (capturePoint != null) {
            return capturePoint;
        }
        controller.sendOperation("自动处置动作下发警告: 捕获点配置缺失，使用目标当前位置作为捕获点。");
        log.warn("自动处置捕获点缺失，回退到目标当前位置: targetId={}", target.id());
        return new CountermeasureConfigService.GeoPoint(
                toDouble(target.snapshot().longitude()),
                toDouble(target.snapshot().latitude()),
                toDouble(target.snapshot().altitude())
        );
    }

    private String resolveExecutableDeceptionDeviceId(String actionLabel) {
        CountermeasureOmcFlag omcFlag = countermeasureConfigService.getOmcFlag();
        if (countermeasureDeviceDirectoryService.resolveExecutableSpoofingDevices(omcFlag).isEmpty()) {
            controller.sendOperation("自动处置动作跳过下发: 当前OMC策略下没有可执行的诱骗设备。");
            log.warn("跳过{}: omcFlag={}, reason=没有可执行诱骗设备", actionLabel, omcFlag);
            return null;
        }
        String deceptionDeviceId = countermeasureDeviceDirectoryService.resolveDeceptionRuntimeDeviceId();
        if (deceptionDeviceId == null) {
            controller.sendOperation("自动处置动作跳过下发: 未找到诱骗设备，无法执行" + actionLabel + "动作。");
            log.warn("跳过{}: 未找到TALENT设备", actionLabel);
            return null;
        }
        return ensureDeceptionConnected(deceptionDeviceId) ? deceptionDeviceId : null;
    }

    private DeviceId buildDeviceId(String deviceId) {
        return DeviceId.newBuilder()
                .setDeviceId(deviceId)
                .build();
    }

    private boolean ensureDeceptionConnected(String deceptionDeviceId) {
        try {
            ConnectionStatus status = talentService.isConnected(buildDeviceId(deceptionDeviceId));
            if (!status.getConnected()) {
                controller.sendOperation("自动处置动作下发失败: 诱骗设备未连接，无法执行诱骗相关动作。");
                log.warn("跳过诱骗动作: TALENT设备未连接, deviceId={}", deceptionDeviceId);
                return false;
            }
            return true;
        } catch (Exception e) {
            controller.sendOperation("自动处置动作下发失败: 无法获取诱骗设备连接状态，无法执行诱骗相关动作。");
            log.error("校验TALENT设备连接状态失败: deviceId={}, error={}", deceptionDeviceId, e.getMessage(), e);
            return false;
        }
    }

    private void stopActiveExecutionLocked(String reason) {
        if (activeExecutionState == null) {
            return;
        }

        try {
            switch (activeExecutionState.action()) {
                case UAV_ATTACK_AUTO -> {
                    if (!activeExecutionState.startedJammingExecutions().isEmpty()) {
                        controller.sendOperation("自动处置收口开始: 干扰设备停止发送。");
                        for (StartedJammingExecution execution : activeExecutionState.startedJammingExecutions()) {
                            sendCommands(execution.stopCommands());
                        }
                    }
                }
                case DECEPTION_DRIVE, DECEPTION_CAPTURE, DECEPTION_DEFENSE, DECEPTION_INTERFERENCE, DECEPTION_NO_FLY -> {
                    if (activeExecutionState.deceptionDeviceId() != null) {
                        controller.sendOperation("自动处置收口开始: 诱骗设备停止发送。");
                        talentService.stopLaunch(buildDeviceId(activeExecutionState.deceptionDeviceId()));
                    }
                }
                default -> {
                    // no-op
                }
            }
            controller.sendOperation("自动处置收口完成。");
            log.info("自动处置动作收口完成: action={}, targetId={}, reason={}",
                    activeExecutionState.action(), activeExecutionState.targetId(), reason);
        } catch (Exception e) {
            controller.sendOperation("自动处置收口失败: " + e.getMessage());
            log.error("自动处置动作收口失败: {}", e.getMessage(), e);
        } finally {
            activeExecutionState = null;
        }
    }

    private int calculatePowerDbm(BigDecimal rangeMeters) {
        double distanceKm = rangeMeters == null ? DEFAULT_DISTANCE_KM : Math.max(rangeMeters.doubleValue() / 1000.0D, MIN_DISTANCE_KM);
        double power = 40.0D + 20.0D * Math.log10(distanceKm / 10.0D);
        int finalPowerDbm = (int) Math.round(Math.clamp(power, 0.0D, 40.0D));
        // 现场联调约定：当最终计算功率低于 6dBm 时，统一按 6dBm 下发，避免设备收到过低功率。
        if (finalPowerDbm < MIN_TRANSMIT_POWER_DBM) {
            finalPowerDbm = MIN_TRANSMIT_POWER_DBM;
        }
        controller.sendOperation("目标距离计算完成: range=" + distanceKm + "km, 计算功率=" + power + "dBm, 最终下发功率=" + finalPowerDbm + "dBm。");
        log.info("目标距离计算完成: rangeMeters={}, distanceKm={}, rawPowerDbm={}, finalPowerDbm={}",
                rangeMeters, distanceKm, power, finalPowerDbm);
        return finalPowerDbm;
    }

    private double calculateBearingDegrees(double fromLat, double fromLon, double toLat, double toLon) {
        double fromLatRad = Math.toRadians(fromLat);
        double toLatRad = Math.toRadians(toLat);
        double deltaLonRad = Math.toRadians(toLon - fromLon);
        double y = Math.sin(deltaLonRad) * Math.cos(toLatRad);
        double x = Math.cos(fromLatRad) * Math.sin(toLatRad)
                - Math.sin(fromLatRad) * Math.cos(toLatRad) * Math.cos(deltaLonRad);
        double degrees = Math.toDegrees(Math.atan2(y, x));
        controller.sendOperation("驱离方位角计算完成: from=[" + fromLat + ", " + fromLon + "], to=[" + toLat + ", " + toLon + "], angle=" + degrees + "度。");
        log.info("驱离方位角计算完成: fromLat={}, fromLon={}, toLat={}, toLon={}, y={}, x={}, angleDegrees={}",
                fromLat, fromLon, toLat, toLon, y, x, degrees);
        return normalizeDegrees(degrees);
    }

    private double normalizeDegrees(double angle) {
        double normalized = angle % 360.0D;
        return normalized < 0 ? normalized + 360.0D : normalized;
    }

    private double toDouble(BigDecimal value) {
        return value == null ? 0.0D : value.doubleValue();
    }

    private record StartedJammingExecution(
            CountermeasureDeviceDirectoryService.CountermeasureRuntimeDevice device,
            String description,
            List<CountermeasureNettyCommand> stopCommands
    ) {
    }

    private record ActiveExecutionState(
            CountermeasureAction action,
            String targetId,
            String deceptionDeviceId,
            List<StartedJammingExecution> startedJammingExecutions
    ) {
    }
}

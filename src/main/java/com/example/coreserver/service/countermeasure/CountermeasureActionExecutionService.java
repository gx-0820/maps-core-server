package com.example.coreserver.service.countermeasure;

import com.example.coreserver.controller.OperationSseController;
import com.example.coreserver.entity.countermeasure.CountermeasureAction;
import com.example.coreserver.grpc.common.DeviceId;
import com.example.coreserver.grpc.talent.CaptureRequest;
import com.example.coreserver.grpc.talent.ConnectionStatus;
import com.example.coreserver.grpc.talent.DriveAngleRequest;
import com.example.coreserver.grpc.talent.PositionRequest;
import com.example.coreserver.grpc.talent.TransmitPowerRequest;
import com.example.coreserver.grpc.uav.AttackAutoRequest;
import com.example.coreserver.service.device.ConfigService;
import com.example.coreserver.service.device.TalentService;
import com.example.coreserver.service.device.UavService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 自动处置动作下发与收口执行器。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CountermeasureActionExecutionService {

    private static final double MIN_DISTANCE_KM = 0.1D;
    private static final double DEFAULT_DISTANCE_KM = 1.0D;

    private final CountermeasureConfigService countermeasureConfigService;
    private final ConfigService configService;
    private final UavService uavService;
    private final TalentService talentService;
    private final OperationSseController controller;

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
        CountermeasureDevices devices = resolveDevices();
        synchronized (actionLock) {
            executePlanLocked(action, target, reason, devices);
        }
    }

    private CountermeasureDevices resolveDevices() {
        try {
            com.example.coreserver.grpc.config.DeviceConfig config = configService.getConfig();
            String electricDeviceId = null;
            String deceptionDeviceId = null;
            for (com.example.coreserver.grpc.config.Device device : config.getDevicesList()) {
                String type = device.hasDeviceType() ? device.getDeviceType() : null;
                if (electricDeviceId == null && "ELECTRIC_INVESTIGATION".equals(type) && device.hasDeviceId()) {
                    electricDeviceId = device.getDeviceId();
                }
                if (deceptionDeviceId == null && "TALENT".equals(type) && device.hasDeviceId()) {
                    deceptionDeviceId = device.getDeviceId();
                }
            }
            controller.sendOperation("自动处置设备解析完成: 电侦设备Id=" + (electricDeviceId != null ? electricDeviceId : "未找到") +
                    ", 诱骗干扰Id=" + (deceptionDeviceId != null ? deceptionDeviceId : "未找到"));
            log.info("自动处置设备解析完成: electricDeviceId={}, deceptionDeviceId={}", electricDeviceId, deceptionDeviceId);
            return new CountermeasureDevices(electricDeviceId, deceptionDeviceId);
        } catch (Exception e) {
            controller.sendOperation("自动处置设备解析失败: " + e.getMessage());
            log.error("自动处置设备解析失败: {}", e.getMessage(), e);
            return new CountermeasureDevices(null, null);
        }
    }

    private void executePlanLocked(
            CountermeasureAction action,
            CountermeasureRoundTargetService.AssessedTarget target,
            String reason,
            CountermeasureDevices devices
    ) {
        controller.sendOperation("开始执行自动处置动作: 对象Id => " + target.id());
        log.info("开始执行自动处置动作: action={}, targetId={}, source={}, threatLevel={}, score={}, reason={}",
                action, target.id(), target.snapshot().dataSource(), target.threatLevel(), target.threatScore(), reason);
        switch (action) {
            case UAV_ATTACK_AUTO -> {
                if (devices.electricDeviceId() == null) {
                    controller.sendOperation("自动处置动作跳过下发: 未找到电侦设备，无法执行电侦自动打击动作。");
                    log.warn("跳过电侦自动打击: 未找到ELECTRIC_INVESTIGATION设备");
                    return;
                }
                uavService.setAttackAuto(AttackAutoRequest.newBuilder()
                        .setDeviceId(devices.electricDeviceId())
                        .setIsCancel(false)
                        .build());
                activeExecutionState = new ActiveExecutionState(action, target.id(), devices.electricDeviceId(), null);
                controller.sendOperation("自动处置动作下发: 电侦 AutoAttack 指令已发送，目标将被自动打击，等待打击完成。");
                log.info("自动处置动作已下发: action=UAV_ATTACK_AUTO, targetId={}, reason={}", target.id(), reason);
            }
            case DECEPTION_DRIVE -> {
                if (devices.deceptionDeviceId() == null) {
                    controller.sendOperation("自动处置动作跳过下发: 未找到诱骗干扰设备，无法执行诱骗驱离动作。");
                    log.warn("跳过诱骗驱离: 未找到TALENT设备");
                    return;
                }
                if (!ensureDeceptionConnected(devices.deceptionDeviceId())) {
                    return;
                }
                int powerDbm = calculatePowerDbm(target.snapshot().range());
                double angle = calculateDriveAngle(target);
                talentService.sendTransmitPowerCommand(TransmitPowerRequest.newBuilder()
                        .setDeviceId(devices.deceptionDeviceId())
                        .setPower(powerDbm)
                        .build());
                talentService.sendDriveAngleCommand(DriveAngleRequest.newBuilder()
                        .setDeviceId(devices.deceptionDeviceId())
                        .setAngle(angle)
                        .build());
                activeExecutionState = new ActiveExecutionState(action, target.id(), null, devices.deceptionDeviceId());
                controller.sendOperation("自动处置动作下发: 诱骗驱离指令已发送，目标将被诱骗向远离地图中心的方向驱离，功率: " + powerDbm + ", 角度: " + angle);
                log.info("自动处置动作已下发: action=DECEPTION_DRIVE, targetId={}, angle={}, power={}, reason={}",
                        target.id(), angle, powerDbm, reason);
            }
            case DECEPTION_CAPTURE -> {
                if (devices.deceptionDeviceId() == null) {
                    controller.sendOperation("自动处置动作跳过下发: 未找到诱骗干扰设备，无法执行诱骗捕获动作。");
                    log.warn("跳过诱骗捕获: 未找到TALENT设备");
                    return;
                }
                if (!ensureDeceptionConnected(devices.deceptionDeviceId())) {
                    return;
                }
                int powerDbm = calculatePowerDbm(target.snapshot().range());
                CountermeasureConfigService.GeoPoint capturePoint = resolveCapturePoint(target);
                int altitude = (int) Math.round(capturePoint.altitude());
                double latitude = capturePoint.latitude();
                double longitude = capturePoint.longitude();

                talentService.sendTransmitPowerCommand(TransmitPowerRequest.newBuilder()
                        .setDeviceId(devices.deceptionDeviceId())
                        .setPower(powerDbm)
                        .build());
                talentService.sendBootstrapPositionCommand(PositionRequest.newBuilder()
                        .setDeviceId(devices.deceptionDeviceId())
                        .setLatitude(latitude)
                        .setLongitude(longitude)
                        .setAltitude(altitude)
                        .build());
                talentService.sendCaptureCommand(CaptureRequest.newBuilder()
                        .setDeviceId(devices.deceptionDeviceId())
                        .setLatitude(latitude)
                        .setLongitude(longitude)
                        .setAltitude(altitude)
                        .setSimulationLevel(0)
                        .setCaptureType(false)
                        .build());
                activeExecutionState = new ActiveExecutionState(action, target.id(), null, devices.deceptionDeviceId());
                controller.sendOperation("自动处置动作下发: 诱骗捕获指令已发送，目标将被诱骗至捕获点，等待捕获完成。");
                log.info("自动处置动作已下发: action=DECEPTION_CAPTURE, targetId={}, power={}, capturePoint=[{}, {}, {}], reason={}",
                        target.id(), powerDbm, longitude, latitude, altitude, reason);
            }
            default -> {
                controller.sendOperation("自动处置动作下发: 识别到无需下发设备指令的处置动作，系统将继续监控目标状态变化。");
                log.info("本轮动作解析结果为{}，无需下发设备指令", action);
            }
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

    private boolean ensureDeceptionConnected(String deceptionDeviceId) {
        try {
            ConnectionStatus status = talentService.isConnected(DeviceId.newBuilder().setDeviceId(deceptionDeviceId).build());
            if (!status.getConnected()) {
                controller.sendOperation("自动处置动作下发失败: 诱骗干扰设备未连接，无法执行诱骗相关动作。");
                log.warn("跳过诱骗动作: TALENT设备未连接, deviceId={}", deceptionDeviceId);
                return false;
            }
            return true;
        } catch (Exception e) {
            controller.sendOperation("自动处置动作下发失败: 无法获取诱骗干扰设备连接状态，无法执行诱骗相关动作。");
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
                    if (activeExecutionState.electricDeviceId() != null) {
                        controller.sendOperation("自动处置收口开始: 电侦 AutoAttack 停止。");
                        uavService.setAttackAuto(AttackAutoRequest.newBuilder()
                                .setDeviceId(activeExecutionState.electricDeviceId())
                                .setIsCancel(true)
                                .build());
                    }
                }
                case DECEPTION_DRIVE, DECEPTION_CAPTURE -> {
                    if (activeExecutionState.deceptionDeviceId() != null) {
                        controller.sendOperation("自动处置收口开始: 诱骗设备停止发送。");
                        talentService.stopLaunch(DeviceId.newBuilder()
                                .setDeviceId(activeExecutionState.deceptionDeviceId())
                                .build());
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
        controller.sendOperation("目标距离计算完成: range=" + distanceKm + "km, 计算得到的发射功率=" + power + "dBm。");
        log.info("目标距离计算完成: rangeMeters={}, distanceKm={}, powerDbm={}", rangeMeters, distanceKm, power);
        return (int) Math.round(Math.clamp(power, 0.0D, 40.0D));
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

    private record CountermeasureDevices(
            String electricDeviceId,
            String deceptionDeviceId
    ) {
    }

    private record ActiveExecutionState(
            CountermeasureAction action,
            String targetId,
            String electricDeviceId,
            String deceptionDeviceId
    ) {
    }
}

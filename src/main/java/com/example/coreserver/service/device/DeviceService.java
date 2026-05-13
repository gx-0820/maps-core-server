package com.example.coreserver.service.device;

import com.example.coreserver.controller.DeceptionController;
import com.example.coreserver.controller.PhotoelectricController;
import com.example.coreserver.entity.countermeasure.CountermeasureEvent;
import com.example.coreserver.entity.countermeasure.CountermeasureType;
import com.example.coreserver.exception.DeviceBusyException;
import com.example.coreserver.exception.DeviceOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {
    private final PhotoelectricController photoelectricController;
    private final DeceptionController deceptionController;
    private final DeviceParamBuilder paramBuilder;
    private final DeviceLockManager lockManager;
    private final ApplicationEventPublisher eventPublisher;

    // 设备操作执行映射（新增操作类型细分）
    private static final Map<CountermeasureType, TriConsumer<DeviceService, String, String>> OPERATIONS = Map.of(
            CountermeasureType.LASER, (service, params, type) -> service.executeLaser(params, type),
//            CountermeasureType.INTERFERENCE, (service, params, type) -> service.executeInterference(params, type),
            CountermeasureType.DECEPTION, (service, params, type) -> service.executeDeception(params, type)
    );

    @FunctionalInterface
    interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    public String executeCountermeasure(CountermeasureType type) {
        // 发布设备锁定事件
        publishDeviceEvent(type, CountermeasureEvent.EventType.DEVICE_LOCKED,
                "尝试获取设备锁", null);

        if (!lockManager.acquireLock(type)) {
            publishDeviceEvent(type, CountermeasureEvent.EventType.OPERATION_FAILED,
                    "设备忙", Map.of("status", "busy"));
            throw new DeviceBusyException(type.name() + "设备忙");
        }
        publishDeviceEvent(type, CountermeasureEvent.EventType.DEVICE_LOCKED,
                "设备锁已获取", null);

        try {
            // 参数生成事件
            String params = generateRequestParams(type);
            publishDeviceEvent(type, CountermeasureEvent.EventType.PARAM_GENERATED,
                    "生成设备参数", Map.of("parameters", params));

            // 执行具体操作（支持细分操作类型）
            OPERATIONS.get(type).accept(this, params, type.name());

            // 操作成功事件
            publishDeviceEvent(type, CountermeasureEvent.EventType.OPERATION_SUCCESS,
                    "操作成功完成", null);
            return type.name() + "操作成功";
        } catch (Exception e) {
            // 操作失败事件
            publishDeviceEvent(type, CountermeasureEvent.EventType.OPERATION_FAILED,
                    "操作失败", Map.of("error", e.getMessage()));
            log.error("设备操作失败", e);
            return type.name() + "操作失败: " + e.getMessage();
        } finally {
            lockManager.releaseLock(type);
            publishDeviceEvent(type, CountermeasureEvent.EventType.DEVICE_LOCKED,
                    "设备锁已释放", null);
        }
    }

    private void executeLaser(String params, String deviceType) {
        try {
            // 激光指令发送事件
            publishDeviceEvent(CountermeasureType.LASER, CountermeasureEvent.EventType.COMMAND_SENT,
                    "发送激光控制指令", Map.of("command", "LAUNCH", "params", params));

            ResponseEntity<?> response = photoelectricController.launchOrStop(params);
            validateResponse(response, "激光设备");

            // 设备响应事件
            publishDeviceEvent(CountermeasureType.LASER, CountermeasureEvent.EventType.DEVICE_RESPONSE,
                    "接收设备响应", Map.of("status", response.getStatusCode()));
        } catch (DeviceOperationException e) {
            publishDeviceEvent(CountermeasureType.LASER, CountermeasureEvent.EventType.OPERATION_FAILED,
                    "激光操作异常", Map.of("error", e.getMessage()));
            throw e;
        }
    }

//    private void executeInterference(String params, String deviceType) {
//        try {
//            // 干扰指令发送事件
//            publishDeviceEvent(CountermeasureType.INTERFERENCE, CountermeasureEvent.EventType.COMMAND_SENT,
//                    "设置干扰模式", Map.of("mode", "GUIDANCE", "params", params));
//
//            ResponseEntity<?> response = photoelectricController.setGuidanceMode(params);
//            validateResponse(response, "干扰设备");
//
//            publishDeviceEvent(CountermeasureType.INTERFERENCE, CountermeasureEvent.EventType.DEVICE_RESPONSE,
//                    "接收设备响应", Map.of("status", response.getStatusCode()));
//        } catch (DeviceOperationException e) {
//            publishDeviceEvent(CountermeasureType.INTERFERENCE, CountermeasureEvent.EventType.OPERATION_FAILED,
//                    "干扰操作异常", Map.of("error", e.getMessage()));
//            throw e;
//        }
//    }

    private void executeDeception(String params, String deviceType) {
        try {
            String[] parts = params.split("\\|");

            // 分步推送诱骗操作事件
            publishDeviceEvent(CountermeasureType.DECEPTION, CountermeasureEvent.EventType.COMMAND_SENT,
                    "更新连接设置", Map.of("setting", parts[0]));
            deceptionController.updateConnectSetting(parts[0]);

            publishDeviceEvent(CountermeasureType.DECEPTION, CountermeasureEvent.EventType.COMMAND_SENT,
                    "更新诱骗指令", Map.of("command", parts[1]));
            deceptionController.updateCommand(parts[1]);

            publishDeviceEvent(CountermeasureType.DECEPTION, CountermeasureEvent.EventType.DEVICE_RESPONSE,
                    "诱骗操作完成", null);
        } catch (Exception e) {
            publishDeviceEvent(CountermeasureType.DECEPTION, CountermeasureEvent.EventType.OPERATION_FAILED,
                    "诱骗操作失败", Map.of("error", e.getMessage()));
            throw new DeviceOperationException("诱骗设备操作失败: " + e.getMessage());
        }
    }

    private void publishDeviceEvent(CountermeasureType type,
                                    CountermeasureEvent.EventType eventType,
                                    String message,
                                    Map<String, Object> params) {
        eventPublisher.publishEvent(CountermeasureEvent.builder()
                .type(eventType)
                .deviceType(type.name())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .params(params)
                .build());
    }

    private String generateRequestParams(CountermeasureType type) {
        return switch (type) {
            case LASER -> paramBuilder.buildLaserParams();
            case INTERFERENCE -> paramBuilder.buildInterferenceParams();
            case DECEPTION -> paramBuilder.buildDeceptionParams();
        };
    }

    private void executeLaser(String params) {
        ResponseEntity<?> response = photoelectricController.launchOrStop(params);
        validateResponse(response, "激光设备");
    }

//    private void executeInterference(String params) {
//        ResponseEntity<?> response = photoelectricController.setGuidanceMode(params);
//        validateResponse(response, "干扰设备");
//    }

    private void executeDeception(String params) {
        // 分步执行诱骗操作
        String[] parts = params.split("\\|");
        deceptionController.updateConnectSetting(parts[0]);
        deceptionController.updateCommand(parts[1]);
    }

    private void validateResponse(ResponseEntity<?> response, String deviceName) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new DeviceOperationException(deviceName + "返回异常状态码: " + response.getStatusCodeValue());
        }
    }
}
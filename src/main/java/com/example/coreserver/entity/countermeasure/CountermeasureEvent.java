package com.example.coreserver.entity.countermeasure;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class CountermeasureEvent {
    public enum EventType {
        DEVICE_LOCKED,       // 设备锁定
        PARAM_GENERATED,     // 参数生成
        COMMAND_SENT,        // 指令发送
        DEVICE_RESPONSE,     // 设备响应
        OPERATION_SUCCESS,   // 操作成功
        OPERATION_FAILED,    // 操作失败
        MODE_CHECKED,        // 模式检查
        THREAT_ASSESSED,     // 威胁评估
        DEVICE_SELECTED      // 设备选择
    }

    private EventType type;
    private String deviceType;
    private String message;
    private long timestamp;
    private Map<String, Object> params;
}
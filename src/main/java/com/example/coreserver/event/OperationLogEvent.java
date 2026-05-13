package com.example.coreserver.event;

import com.example.coreserver.entity.log.OperationLog;
import org.springframework.context.ApplicationEvent;

/**
 * @author lord
 * @date 2025/4/4
 * @description
 */
public class OperationLogEvent extends ApplicationEvent {

    public OperationLogEvent(OperationLog operationLog) {
        super(operationLog);
    }
}
package com.example.coreserver.listener;

import com.example.coreserver.entity.log.ExceptionLog;
import com.example.coreserver.entity.log.OperationLog;
import com.example.coreserver.event.ExceptionLogEvent;
import com.example.coreserver.event.OperationLogEvent;
import com.example.coreserver.mapper.ExceptionLogMapper;
import com.example.coreserver.mapper.OperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author Lord Camelot
 * @date 2025/4/4
 * @description
 */
@Component
public class Listener {
    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private ExceptionLogMapper exceptionLogMapper;

    @Async
    @EventListener(OperationLogEvent.class)
    public void saveOperationLog(OperationLogEvent operationLogEvent) {
        operationLogMapper.insert((OperationLog) operationLogEvent.getSource());
    }

    @Async
    @EventListener(ExceptionLogEvent.class)
    public void saveExceptionLog(ExceptionLogEvent exceptionLogEvent) {
        exceptionLogMapper.insert((ExceptionLog) exceptionLogEvent.getSource());
    }
}
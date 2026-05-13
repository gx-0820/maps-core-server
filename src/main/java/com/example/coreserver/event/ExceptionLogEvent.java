package com.example.coreserver.event;


import com.example.coreserver.entity.log.ExceptionLog;
import org.springframework.context.ApplicationEvent;

/**
 * @author Lord Camelot
 * @date 2025/4/4
 * @description
 */
public class ExceptionLogEvent extends ApplicationEvent {
    public ExceptionLogEvent(ExceptionLog exceptionLog) {
        super(exceptionLog);
    }
}
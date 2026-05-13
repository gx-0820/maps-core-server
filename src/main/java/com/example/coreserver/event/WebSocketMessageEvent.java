package com.example.coreserver.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WebSocketMessageEvent extends ApplicationEvent {
    private final String message;

    public WebSocketMessageEvent(String message) {
        super(message);
        this.message = message;
    }
} 
package com.example.coreserver.config;

import com.example.coreserver.service.socket.ObjectDetectionVideoStreamHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class ObjectDetectionVideoWebSocketConfig implements WebSocketConfigurer {

    private final ObjectDetectionVideoStreamHandler objectDetectionVideoStreamHandler;

    public ObjectDetectionVideoWebSocketConfig(ObjectDetectionVideoStreamHandler objectDetectionVideoStreamHandler) {
        this.objectDetectionVideoStreamHandler = objectDetectionVideoStreamHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(objectDetectionVideoStreamHandler, "/ws/video-stream/{deviceId}")
                .setAllowedOrigins("*");
    }
}
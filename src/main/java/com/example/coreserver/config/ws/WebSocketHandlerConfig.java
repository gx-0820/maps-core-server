package com.example.coreserver.config.ws;

import com.example.coreserver.service.socket.PhotoelectricVideoStreamHandler;
import com.example.coreserver.service.socket.ServerDataClientHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketHandlerConfig implements WebSocketConfigurer {

    private final PhotoelectricVideoStreamHandler videoStreamHandler;
    private final ServerDataClientHandler serverDataClientHandler;
//    private final GuidanceDataClientHandler guidanceDataClientHandler;

    public WebSocketHandlerConfig(PhotoelectricVideoStreamHandler videoStreamHandler, ServerDataClientHandler serverDataClientHandler) {
        this.videoStreamHandler = videoStreamHandler;
        this.serverDataClientHandler = serverDataClientHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册视频流处理器，路径格式：/ws/photoelectric-video-stream/{deviceId}
        registry.addHandler(videoStreamHandler, "/ws/photoelectric-video-stream/{deviceId}")
                .setAllowedOriginPatterns("*");  // 允许所有来源，生产环境应该限制
//                .withSockJS();  // 添加SockJS支持，提供更好的兼容性


        registry.addHandler(serverDataClientHandler, "/ws/server/data")
                .setAllowedOriginPatterns("*");  // 允许所有来源，生产环境应该限制


//        registry.addHandler(guidanceDataClientHandler, "/ws/client/data")
//                .setAllowedOriginPatterns("*");  // 允许所有来源，生产环境应该限制
    }
}

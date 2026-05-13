package com.example.coreserver.service.socket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PhotoelectricVideoStreamHandler extends BinaryWebSocketHandler {

    // 维护设备ID与前端会话的映射（线程安全）
    private static final ConcurrentHashMap<String, Set<WebSocketSession>> deviceFrontendSessions = new ConcurrentHashMap<>();

    /** 供ClientHandler调用的转发方法 **/
    public void forwardToFrontend(String deviceId, ByteBuffer videoData) {
        Set<WebSocketSession> sessions = deviceFrontendSessions.getOrDefault(deviceId, Collections.emptySet());
        if (sessions.isEmpty()) {
            log.debug("没有找到设备ID对应的前端会话 - DeviceID={}", deviceId);
            return;
        }
        
        log.debug("开始转发数据到前端 - DeviceID={}, SessionCount={}, DataSize={}", 
            deviceId, sessions.size(), videoData.remaining());
            
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    // 注意：必须复制缓冲区避免竞争
                    ByteBuffer copy = ByteBuffer.allocate(videoData.remaining());
                    copy.put(videoData.slice());
                    copy.flip();
                    session.sendMessage(new BinaryMessage(copy));
                    log.debug("成功转发数据到前端 - DeviceID={}, SessionID={}", deviceId, session.getId());
                } else {
                    log.warn("会话已关闭，无法转发 - DeviceID={}, SessionID={}", deviceId, session.getId());
                }
            } catch (IOException e) {
                log.error("转发至前端失败 - DeviceID={}, SessionID={}, Error={}", 
                    deviceId, session.getId(), e.getMessage());
            }
        });
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String deviceId = extractDeviceId(session);
        log.info("前端WebSocket连接建立 - DeviceID={}, SessionID={}, RemoteAddress={}, URI={}", 
            deviceId, session.getId(), session.getRemoteAddress(), session.getUri());
        
        deviceFrontendSessions.computeIfAbsent(deviceId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        // 不需要处理前端发来的消息（仅转发）
        log.debug("收到前端消息 - SessionID={}, MessageSize={}", session.getId(), message.getPayloadLength());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String deviceId = extractDeviceId(session);
        log.info("前端WebSocket连接关闭 - DeviceID={}, SessionID={}, Status={}, Reason={}", 
            deviceId, session.getId(), status.getCode(), status.getReason());
        
        deviceFrontendSessions.getOrDefault(deviceId, Collections.emptySet()).remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String deviceId = extractDeviceId(session);
        log.error("WebSocket传输错误 - DeviceID={}, SessionID={}, Error={}", 
            deviceId, session.getId(), exception.getMessage(), exception);
    }

    /** 统一设备ID提取方式 **/
    private String extractDeviceId(WebSocketSession session) {
        try {
            // 路径格式：/ws/photoelectric-video-stream/{deviceId}
            String[] segments = session.getUri().getPath().split("/");
            // 获取最后一个非空段作为设备ID
            String deviceId = "unknown";
            for (int i = segments.length - 1; i >= 0; i--) {
                if (!segments[i].isEmpty()) {
                    deviceId = segments[i];
                    break;
                }
            }
            log.debug("从URI提取设备ID - URI={}, DeviceID={}", session.getUri(), deviceId);
            return deviceId;
        } catch (Exception e) {
            log.error("提取设备ID失败 - URI={}, Error={}", session.getUri(), e.getMessage());
            return "unknown";
        }
    }
}
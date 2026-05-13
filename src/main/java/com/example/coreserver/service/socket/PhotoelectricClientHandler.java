package com.example.coreserver.service.socket;

import com.example.coreserver.service.business.FrontendSessionManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class PhotoelectricClientHandler extends BinaryWebSocketHandler {

    // 添加缓冲区大小配置
    private static final int MAX_MESSAGE_SIZE = 256 * 1024; // 256KB

    @Getter
    private volatile boolean connected = false;

    @Autowired
    private PhotoelectricVideoStreamHandler videoStreamHandler;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.connected = true;

        // 关键配置：设置消息缓冲区大小并启用分片支持
        session.setBinaryMessageSizeLimit(MAX_MESSAGE_SIZE);

        log.info("成功连接到 WebSocket 客户端 (缓冲区大小: {} bytes)", MAX_MESSAGE_SIZE);
        session.sendMessage(new TextMessage("WebSocket connection established"));
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            // 添加缓冲区溢出检查
            if (message.getPayloadLength() > MAX_MESSAGE_SIZE) {
                log.warn("收到超大数据包: {} bytes (限制: {} bytes)",
                        message.getPayloadLength(), MAX_MESSAGE_SIZE);
                return;
            }

            ByteBuffer buffer = message.getPayload();
            buffer.order(ByteOrder.LITTLE_ENDIAN); // 服务器头部使用小端字节序

            // 验证最小头部长度（8 + 1 + 0 + 4 + 4 = 17字节）
            if (buffer.remaining() < 17) {
                log.error("无效消息: 数据长度不足");
                return;
            }

            // 读取时间戳（8字节）
            long timestamp = buffer.getLong();

            // 读取设备ID长度（1字节无符号）
            int deviceIdLength = buffer.get() & 0xFF;

            // 验证设备ID长度有效性
            if (buffer.remaining() < deviceIdLength + 8) { // 后续需要4+4字节的宽高
                log.error("无效的设备ID长度: {}", deviceIdLength);
                return;
            }

            // 读取设备ID
            byte[] deviceIdBytes = new byte[deviceIdLength];
            if (deviceIdLength > 0) {
                buffer.get(deviceIdBytes);
            }
            String deviceId = new String(deviceIdBytes, StandardCharsets.UTF_8);

            // 读取图像尺寸
            int imgWidth = buffer.getInt();
            int imgHeight = buffer.getInt();

            // 读取图像数据
            byte[] frameData = new byte[buffer.remaining()];
            buffer.get(frameData);

//            log.info("接收到视频帧 - 时间戳: {} | 设备: {} | 尺寸: {}x{} | 数据大小: {} bytes",
//                    timestamp, deviceId, imgWidth, imgHeight, frameData.length);

            // 构建转发数据包
            byte[] forwardData = buildForwardMessage(timestamp, deviceId, imgWidth, imgHeight, frameData);
            
            // 使用 ByteBuffer 包装数据
            ByteBuffer forwardBuffer = ByteBuffer.wrap(forwardData);
            
            // 通过 PhotoelectricVideoStreamHandler 转发给前端
            videoStreamHandler.forwardToFrontend(deviceId, forwardBuffer);
        } catch (Exception e) {
            log.error("解析二进制消息失败: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        this.connected = false;
        log.warn("连接断开: {} | 状态码: {}", status.getReason(), status.getCode());
    }

    // 构建转发消息格式
    private byte[] buildForwardMessage(long timestamp, String deviceId,
                                       int width, int height, byte[] frameData) {
        ByteBuffer buffer = ByteBuffer.allocate(17 + deviceId.length() + frameData.length)
                .order(ByteOrder.LITTLE_ENDIAN);

        buffer.putLong(timestamp);
        buffer.put((byte)deviceId.length());
        buffer.put(deviceId.getBytes(StandardCharsets.UTF_8));
        buffer.putInt(width);
        buffer.putInt(height);
        buffer.put(frameData);

        return buffer.array();
    }

    // 广播
    private void broadcastToFrontend(byte[] data) {
        BinaryMessage message = new BinaryMessage(data);
        FrontendSessionManager.getSessions().forEach((id, session) -> {
            try {
                if (session.isOpen()) {
                    synchronized (session) { // 线程安全发送
                        session.sendMessage(message);
                    }
                }
            } catch (IOException e) {
                log.error("转发失败: {}", e.getMessage());
            }
        });
    }
}
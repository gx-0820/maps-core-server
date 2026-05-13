package com.example.coreserver.service.socket;

import com.example.coreserver.entity.algorithm.ObjectDetection;
import com.example.coreserver.service.algorithm.AlgorithmDataProcessor;
import com.example.coreserver.service.algorithm.AlgorithmGrpcClient;
import com.example.coreserver.service.algorithm.ImageProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class CameraClientHandler extends BinaryWebSocketHandler {

    // 添加缓冲区大小配置
    private static final int MAX_MESSAGE_SIZE = 256 * 1024; // 256KB

    @Autowired
    private AlgorithmGrpcClient algorithmGrpcClient;

    @Autowired
    private ObjectDetectionVideoStreamHandler videoStreamHandler;
    
    @Autowired
    private AlgorithmDataProcessor algorithmDataProcessor;
    
    @Autowired
    private ImageProcessor imageProcessor;

    @Getter
    private volatile boolean connected = false;

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
            
            // 或者直接使用毫秒数的字符串表示
             String timestampStr = String.valueOf(timestamp);

            
//            log.info("原始时间戳(毫秒): {} | ISO时间字符串: {}",
//                     timestamp, timestampStr);

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

//            log.info("{}", frameData);
//             异步处理
            CompletableFuture.runAsync(() -> {
                try {
                    algorithmGrpcClient.pushImageData(timestampStr, frameData);
                } catch (Exception e) {
                    log.error("Failed to process image data", e);
                }
            });

            // 获取目标检测结果
            ObjectDetection detection = algorithmDataProcessor.getCurrentDetection();

            // 处理视频帧，绘制检测框
            byte[] processedFrameData = frameData;
            if (detection != null && detection.getTargets() != null && !detection.getTargets().isEmpty()) {
                log.debug("应用目标检测框 - 目标数量: {}", detection.getTargets().size());
                processedFrameData = imageProcessor.processDetectionFrame(frameData, detection.getTargets());
            }

            // 构建转发数据包 - 使用原始long类型时间戳
            byte[] forwardData = buildForwardMessage(timestamp, deviceId, imgWidth, imgHeight, processedFrameData);

            // 使用 ByteBuffer 包装数据
            ByteBuffer forwardBuffer = ByteBuffer.wrap(forwardData);

            // 转发给前端
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

//    // 广播到所有测试客户端
//    private void broadcastToTestSessions(byte[] data) {
//        BinaryMessage message = new BinaryMessage(data);
//        testSessions.forEachValue(1, session -> {
//            try {
//                if (session.isOpen()) {
//                    synchronized (session) {
//                        session.sendMessage(message);
//                    }
//                }
//            } catch (IOException e) {
//                log.error("测试端点发送失败: {}", e.getMessage());
//            }
//        });
//    }
}
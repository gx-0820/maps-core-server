package com.example.coreserver.config.ws;

import com.example.coreserver.grpc.camera.DeviceListResponse;
import com.example.coreserver.service.device.CameraService;
import com.example.coreserver.service.socket.CameraClientHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Slf4j
@Configuration
@EnableScheduling
public class CameraClientConfig {

    @Value("${camera.websocket.ws-url}")
    private String serviceAUrl;

    @Autowired
    private CameraService cameraService; // 用于获取设备 ID 的服务

    @Autowired
    private CameraClientHandler serviceAClientHandler; // WebSocket 处理器

    private String deviceId; // 动态设备 ID
    private WebSocketConnectionManager connectionManager; // WebSocket 连接管理器

    /**
     * 初始化时尝试连接（可能无 deviceId）
     */
    @PostConstruct
    public void init() {
        // 尝试从 CameraService 获取设备 ID
        this.deviceId = getDeviceIdFromService();
        if (deviceId != null) {
            startConnection(); // 如果获取到设备 ID，尝试连接
        } else {
            log.warn("未获取到设备ID，延迟连接");
        }
    }

//    /**
//     * 监听设备 ID 更新事件
//     */
//    @EventListener
//    public void handlePanoramicCameraEvent(PanoramicCameraEvent event) {
//        String newDeviceId = event.getDeviceId();
//        if (!newDeviceId.equals(this.deviceId)) { // 如果设备 ID 发生变化
//            this.deviceId = newDeviceId;
//            restartConnection(); // 重启 WebSocket 连接
//        }
//    }

    /**
     * 定时检查连接状态
     */
    @Scheduled(fixedRate = 30000)
    public void checkConnection() {
        if (deviceId != null && (connectionManager == null || !serviceAClientHandler.isConnected())) {
            log.warn("设备ID存在但连接未建立或已断开，尝试重新连接");
            restartConnection();
        }
    }

    /**
     * 从 CameraService 获取设备 ID
     */
    private String getDeviceIdFromService() {
        try {
            // 调用 CameraService 获取设备列表
            DeviceListResponse deviceListResponse = cameraService.getCameraDevices();
            if (deviceListResponse != null) {
                // 返回第一个设备 ID
                return deviceListResponse.getDevices(0).getDeviceId();
            }
        } catch (Exception e) {
            log.error("从 CameraService 获取设备 ID 失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 启动 WebSocket 连接
     */
    private void startConnection() {
        if (deviceId == null) {
            log.warn("设备ID为空，无法启动 WebSocket 连接");
            return;
        }

        // 替换 URL 中的 {deviceId} 占位符
        String fullUrl = serviceAUrl.replace("{deviceId}", deviceId);

        // 创建 WebSocket 客户端
        WebSocketClient client = new StandardWebSocketClient();

        // 初始化连接管理器
        connectionManager = new WebSocketConnectionManager(
                client,
                serviceAClientHandler,
                fullUrl
        );

        // 启动连接
        connectionManager.start();
        log.info("成功连接到服务: {}", fullUrl);
    }

    /**
     * 重启 WebSocket 连接
     */
    private void restartConnection() {
        if (connectionManager != null) {
            connectionManager.stop(); // 停止现有连接
        }
        startConnection(); // 启动新连接
    }
}
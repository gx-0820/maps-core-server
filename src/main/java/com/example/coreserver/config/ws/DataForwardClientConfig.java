package com.example.coreserver.config.ws;

import com.example.coreserver.handler.RadarDataHandler;
import com.example.coreserver.service.DataRadarTargetService;
import com.example.coreserver.service.DataTdoaTargetService;
import com.example.coreserver.service.algorithm.AlgorithmGrpcClient;
import com.example.coreserver.service.business.GeofenceService;
import com.example.coreserver.service.socket.DataForwardClientHandler;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class DataForwardClientConfig {

    @Value("${data.websocket.data-forward-url}")
    private String websocketUrl;

    private Session session;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    // 注入 Spring 管理的服务
    private final ApplicationEventPublisher eventPublisher;
    private final GeofenceService geofenceService;
    private final AlgorithmGrpcClient algorithmGrpcClient;
    private final RadarDataHandler radarDataHandler;

    private ScheduledFuture<?> heartbeatTask;
    private final AtomicBoolean isHeartbeatRunning = new AtomicBoolean(false);

    private final ApplicationContext applicationContext;
//    private final DataRadarTargetService dataRadarTargetService;
//    private final DataTdoaTargetService dataTdoaTargetService;

    public DataForwardClientConfig(
            ApplicationEventPublisher eventPublisher,
            GeofenceService geofenceService,
            AlgorithmGrpcClient algorithmGrpcClient,
            RadarDataHandler radarDataHandler,
            ApplicationContext applicationContext) {
        this.eventPublisher = eventPublisher;
        this.geofenceService = geofenceService;
        this.algorithmGrpcClient = algorithmGrpcClient;
        this.radarDataHandler = radarDataHandler;

        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        // ✅ 关键：在连接前设置静态依赖
        DataForwardClientHandler.setDependencies(
                eventPublisher,
                geofenceService,
                algorithmGrpcClient,
                radarDataHandler,
                applicationContext
        );
        // 设置心跳回调
        System.out.println("✅ DataForwardClientHandler 静态依赖已注入");
        connectWithRetry();
    }

    private void connectWithRetry() {
        executor.scheduleAtFixedRate(this::doConnect, 0, 5, TimeUnit.SECONDS);
    }

    private void doConnect() {
        try {
            if (session == null || !session.isOpen()) {
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                // 使用动态配置的URL
                session = container.connectToServer(
                        DataForwardClientHandler.class,
                        new URI(websocketUrl) // 读取YAML中的URL
                );

                // 启动心跳
                startHeartbeat();
                System.out.println("WebSocket连接成功: " + websocketUrl);
            }
        } catch (Exception e) {
            System.err.println("WebSocket连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 启动心跳机制
     */
    private void startHeartbeat() {
        if (isHeartbeatRunning.getAndSet(true)) {
            return; // 已经运行
        }

        // 停止已有的心跳任务
        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
        }

        heartbeatTask = executor.scheduleAtFixedRate(() -> {
            try {
                if (session != null && session.isOpen()) {

                    // 发送心跳ping消息
                    String pingMsg = "{\"type\":\"ping\",\"timestamp\":" + System.currentTimeMillis() + "}";
                    session.getBasicRemote().sendText(pingMsg);
//                    System.out.println("💓 发送心跳 Ping");
                }
            } catch (Exception e) {
                System.err.println("发送心跳失败: " + e.getMessage());

            }
        }, 10, 5, TimeUnit.SECONDS);

    }


}
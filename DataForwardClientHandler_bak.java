package com.example.coreserver.service.socket;

import com.alibaba.fastjson2.JSONObject;
import com.example.coreserver.entity.Geofence;
import com.example.coreserver.handler.RadarDataHandler;
import com.example.coreserver.service.business.GeofenceService;
import com.example.coreserver.service.algorithm.AlgorithmGrpcClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled; // 导入 @Scheduled
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Component
@ClientEndpoint
public class DataForwardClientHandler_bak {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private GeofenceService geofenceService;

    @Autowired
    private AlgorithmGrpcClient algorithmGrpcClient;

    @Autowired
    private RadarDataHandler radarDataHandler;

//    private static volatile DataForwardClientHandler webSocket;

    // 地理围栏数据的缓存
    private volatile ArrayNode cachedGeofences;
    // 用于线程安全访问缓存的读写锁
    private final ReadWriteLock geofenceCacheLock = new ReentrantReadWriteLock();

    @PostConstruct
    public void init() {
//        webSocket = this;
        log.info("DataForwardClientHandler initialized. ");
        // 应用程序启动时，首次加载地理围栏数据
        refreshGeofenceCache();
    }

    // 定时任务：每隔 5 分钟刷新一次地理围栏缓存 (可根据需要调整时间)
    @Scheduled(fixedRate = 300000) // 300000 毫秒 = 5 分钟
    public void refreshGeofenceCache() {
        geofenceCacheLock.writeLock().lock(); // 获取写锁，用于更新缓存
        try {
            log.info("Refreshing geofence cache...");
            cachedGeofences = buildGeofenceArray(geofenceService.findAll());
            log.info("Geofence cache refreshed successfully.");
        } finally {
            geofenceCacheLock.writeLock().unlock(); // 释放写锁
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("WebSocket connection established: {}", session.getId());
    }

    @OnMessage
    public void onMessage(String message) {
        long startTime = System.currentTimeMillis();
//        try {
//            if (webSocket == null || webSocket.radarDataHandler == null) {
//                log.warn("WebSocket or RadarDataHandler is not initialized yet. Message processing aborted.");
//                return;
//            }
        try {
            // ✅ 直接检查 this.radarDataHandler 是否为 null（虽然理论上不会，但可加防护）
            if (this.radarDataHandler == null) {
                log.warn("RadarDataHandler not injected yet. Message processing aborted.");
                return;
            }
            JsonNode originalData = mapper.readTree(message);
            ObjectNode mergedData = mapper.createObjectNode();
            mergedData.put("timestamp", System.currentTimeMillis());
            mergedData.set("rawData", originalData);

            // 使用缓存的地理围栏数据
            geofenceCacheLock.readLock().lock(); // 获取读锁，用于访问缓存
            try {
                if (cachedGeofences != null) {
                    mergedData.set("geofence", cachedGeofences);
                } else {
                    // 备用方案：如果缓存为空，则从数据库中获取 (理想情况下在初始化后不应该发生)
                    log.warn("Geofence cache is null, fetching from database as fallback.");
                    mergedData.set("geofence", buildGeofenceArray(this.geofenceService.findAll()));
                }
            } finally {
                geofenceCacheLock.readLock().unlock(); // 释放读锁
            }

//            log.info("mergedData: {}", mergedData);
            // 推送数据到 gRPC 服务
//            AlgorithmGrpcClient grpcClient = webSocket.algorithmGrpcClient;
//            grpcClient.PushFusionData(String.valueOf(mergedData));
//            grpcClient.PushTrackData(String.valueOf(mergedData));
            this.algorithmGrpcClient.PushFusionData(String.valueOf(mergedData));
            this.algorithmGrpcClient.PushTrackData(String.valueOf(mergedData));
            // 处理雷达数据
            String type = originalData.get("type").asText();
            if (type.equals("RADAR")) {
                this.radarDataHandler.handleRadarData2Silas(JSONObject.parseObject(message));
            }
        } catch (Exception e) {
            log.error("Message processing failed in {}ms | Error: {}",
                    System.currentTimeMillis() - startTime,
                    e.getMessage(),
                    e);
        }
    }

    private ArrayNode buildGeofenceArray(List<Geofence> geofences) {
        ArrayNode arrayNode = mapper.createArrayNode();
        if (geofences != null) { // 检查 geofences 是否为 null
            geofences.forEach(geofence -> arrayNode.add(
                    mapper.createObjectNode()
                            .put("id", geofence.getId())
                            .put("name", geofence.getName())
                            .put("coreLongitude", geofence.getCoreLongitude())
                            .put("coreLatitude", geofence.getCoreLatitude())
                            .put("coreRadius", geofence.getCoreRadius())
                            .put("bufferRadius", geofence.getBufferRadius())
                            .put("alertRadius", geofence.getAlertRadius())
            ));
        }
        return arrayNode;
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.warn("Connection closed: {} | Reason: {}",
                session.getId(),
                closeReason.getReasonPhrase());
//        webSocket = null;
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        log.error("WebSocket error on session {}: {}",
                session != null ? session.getId() : "null",
                thr.getMessage(),
                thr);
    }
}
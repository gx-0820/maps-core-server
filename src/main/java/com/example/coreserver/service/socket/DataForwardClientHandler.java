package com.example.coreserver.service.socket;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.coreserver.entity.DataOfd;
import com.example.coreserver.entity.DataRadarTarget;
import com.example.coreserver.entity.DataTdoaTarget;
import com.example.coreserver.entity.Geofence;
import com.example.coreserver.handler.RadarDataHandler;
import com.example.coreserver.service.DataOfdService;
import com.example.coreserver.service.DataRadarTargetService;
import com.example.coreserver.service.DataTdoaTargetService;
import com.example.coreserver.service.algorithm.AlgorithmGrpcClient;
import com.example.coreserver.service.business.GeofenceService;
import com.example.coreserver.service.threat.ThreatAssessmentService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import static com.example.coreserver.Constant.*;

@Slf4j
@ClientEndpoint
public class DataForwardClientHandler {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static Map<String, String> maps = new ConcurrentHashMap<>();

    // 静态引用，用于在WebSocket回调中访问Spring管理的服务
    private static ApplicationEventPublisher eventPublisher;
    private static GeofenceService geofenceService;
    private static AlgorithmGrpcClient algorithmGrpcClient;
    private static RadarDataHandler radarDataHandler;


    private static ApplicationContext applicationContext;

    private static DataRadarTargetService dataRadarTargetService;
    private static DataTdoaTargetService dataTdoaTargetService;
    private static DataOfdService dataOfdService;

    private static ThreatAssessmentService threatAssessmentService;
    private static ServerDataClientHandler serverDataClientHandler;

    private static GuidanceDataClientHandler guidanceDataClientHandler;
    private static DataService dataService;

    // 移除静态修饰符，改为实例变量
    private volatile DataForwardClientHandler instance;
    private ArrayNode cachedGeofences;
    private final ReadWriteLock geofenceCacheLock = new ReentrantReadWriteLock();
    private final static Function<Class<?>, Object> function = (clz) -> applicationContext.getBeanProvider(clz).getIfUnique();


    // 无参构造函数，因为@ClientEndpoint类由WebSocket容器实例化
    public DataForwardClientHandler() {
    }

    // 静态方法用于设置Spring管理的依赖
    public static void setDependencies(
            ApplicationEventPublisher eventPublisher,
            GeofenceService geofenceService,
            AlgorithmGrpcClient algorithmGrpcClient,
            RadarDataHandler radarDataHandler,
            ApplicationContext applicationContext
    ) {
        DataForwardClientHandler.applicationContext = applicationContext;
        DataForwardClientHandler.eventPublisher = eventPublisher;
        DataForwardClientHandler.geofenceService = geofenceService;
        DataForwardClientHandler.algorithmGrpcClient = algorithmGrpcClient;
        DataForwardClientHandler.radarDataHandler = radarDataHandler;

        DataForwardClientHandler.dataRadarTargetService = (DataRadarTargetService) function.apply(DataRadarTargetService.class);
        DataForwardClientHandler.dataTdoaTargetService = (DataTdoaTargetService) function.apply(DataTdoaTargetService.class);
        DataForwardClientHandler.dataOfdService = (DataOfdService) function.apply(DataOfdService.class);

        DataForwardClientHandler.threatAssessmentService = (ThreatAssessmentService) function.apply(ThreatAssessmentService.class);
        DataForwardClientHandler.serverDataClientHandler = (ServerDataClientHandler) function.apply(ServerDataClientHandler.class);
        DataForwardClientHandler.guidanceDataClientHandler = (GuidanceDataClientHandler) function.apply(GuidanceDataClientHandler.class);

        DataForwardClientHandler.dataService =  (DataService) function.apply(DataService.class);
    }


    @OnOpen
    public void onOpen(Session session) {
        log.info("WebSocket connection established: {}", session.getId());
        this.instance = this; // 初始化单例引用
        refreshGeofenceCache(); // 启动时加载地理围栏缓存
    }

    // 刷新地理围栏缓存的方法
    public void refreshGeofenceCache() {
        geofenceCacheLock.writeLock().lock();
        try {
            log.info("Refreshing geofence cache...");
            cachedGeofences = buildGeofenceArray(geofenceService.findAll());
            log.info("Geofence cache refreshed successfully.");
        } finally {
            geofenceCacheLock.writeLock().unlock();
        }
    }

    @OnMessage
    public void onMessage(String message) {
        long startTime = System.currentTimeMillis();

        if (instance == null || radarDataHandler == null) {
            log.warn("WebSocket or RadarDataHandler is not initialized yet. Message processing aborted.");
            return;
        }
        // 处理心跳响应
        if (message != null && message.contains("\"type\":\"pong\"")) {
            return;
        }

        DataForwardClientHandler.dataService.handelMessage(message, startTime);

    }



    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.warn("Connection closed: {} | Reason: {}", session.getId(), closeReason.getReasonPhrase());
        instance = null; // 清理单例引用
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        log.error("WebSocket error on session {}: {}",
                session.getId(), thr.getMessage(), thr);
    }




    // 构建地理围栏数组
    private ArrayNode buildGeofenceArray(List<Geofence> geofences) {
        ArrayNode arrayNode = mapper.createArrayNode();
        if (geofences != null) {
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
}
package com.example.coreserver.service.algorithm;

import com.example.coreserver.entity.DataFusionTarget;
import com.example.coreserver.entity.algorithm.*;
import com.example.coreserver.entity.algorithm.db.DataFusionEntity;
import com.example.coreserver.entity.algorithm.db.GeoPositionValidatorEntity;
import com.example.coreserver.entity.algorithm.db.ObjectDetectionEntity;
import com.example.coreserver.entity.algorithm.db.TrackPredictionEntity;
import com.example.coreserver.grpc.algorithm.Result;
import com.example.coreserver.mapper.DataFusionTargetMapper;
import com.example.coreserver.repository.GeoPositionValidatorRepository;
import com.example.coreserver.repository.ObjectDetectionRepository;
import com.example.coreserver.repository.TrackPredictionRepository;
import com.example.coreserver.repository.algorithm.AlgorithmDataFusionRepository;
import com.example.coreserver.service.business.DroneStatsService;
import com.example.coreserver.service.socket.GuidanceDataClientHandler;
import com.example.coreserver.service.socket.ServerDataClientHandler;
import com.example.coreserver.service.threat.ThreatAssessmentService;
import com.example.coreserver.utils.DataBatchUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AlgorithmDataProcessor {

    @Autowired
    private DroneStatsService droneStatsService;

    @Autowired
    private ThreatAssessmentService threatAssessmentService;

    private final Set<Long> observedTargetIds = ConcurrentHashMap.newKeySet();

    private final AlgorithmGrpcClient algorithmClient;
    private final ObjectMapper objectMapper;

    private final ObjectDetectionRepository objectDetectionRepository;
    private final TrackPredictionRepository trackPredictionRepository;
    private final AlgorithmDataFusionRepository dataFusionRepository;
    private final GeoPositionValidatorRepository geoPositionValidatorRepository;
    private final ServerDataClientHandler serverDataClientHandler;
    private final GuidanceDataClientHandler guidanceDataClientHandler;

    // 线程安全的原子引用
    private final AtomicReference<ObjectDetection> currentDetection = new AtomicReference<>();
    private final AtomicReference<List<TrackPrediction>> currentPredictions = new AtomicReference<>(new ArrayList<>());
    private final AtomicReference<List<DataFusionVo>> currentFusions = new AtomicReference<>(new ArrayList<>());
    private final AtomicReference<GeoPositionValidator> currentGeoValidator = new AtomicReference<>();

    // 流连接状态控制
    private final AtomicBoolean fusionStreamActive = new AtomicBoolean(false);
    private final AtomicBoolean trackStreamActive = new AtomicBoolean(false);
    private final AtomicBoolean imageStreamActive = new AtomicBoolean(false);

    // 重连计数器
    private final AtomicInteger fusionReconnectCount = new AtomicInteger(0);
    private final AtomicInteger trackReconnectCount = new AtomicInteger(0);
    private final AtomicInteger imageReconnectCount = new AtomicInteger(0);

    // 常量定义
    private static final int MAX_RECONNECT_ATTEMPTS = 20; // 最大重连次数
    private static final long RECONNECT_DELAY_MS = 5000; // 5秒重连延迟
    private static final long STREAM_RESTART_DELAY_MS = 2000; // 2秒重启延迟

    // 异步存储相关
    private final ExecutorService asyncStorageExecutor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "AsyncStorage-" + System.currentTimeMillis());
        t.setDaemon(true);
        return t;
    });

    // 批量存储相关
    private final BlockingQueue<DataFusionEntity> dataFusionQueue = new LinkedBlockingQueue<>(1000);
    private final BlockingQueue<TrackPredictionEntity> trackPredictionQueue = new LinkedBlockingQueue<>(1000);
    private final BlockingQueue<ObjectDetectionEntity> objectDetectionQueue = new LinkedBlockingQueue<>(1000);
    private final BlockingQueue<GeoPositionValidatorEntity> geoValidatorQueue = new LinkedBlockingQueue<>(1000);

    // 批量存储配置
    private static final int BATCH_SIZE = 50;
    private static final long BATCH_TIMEOUT_MS = 1000; // 1秒超时

    // 存储SSE发射器的列表
    @Getter
    private final Set<SseEmitter> sseEmitters = ConcurrentHashMap.newKeySet();

    private final DataBatchUtils<DataFusionTarget> dataBatchUtils;
    private final DataFusionTargetMapper dataFusionTargetMapper;
    public static final String BUSINESS_KEY = "DataFusionEntity";

    @Autowired
    public AlgorithmDataProcessor(
            AlgorithmGrpcClient algorithmClient,
            ObjectMapper objectMapper,
            ObjectDetectionRepository objectDetectionRepository,
            TrackPredictionRepository trackPredictionRepository,
            AlgorithmDataFusionRepository dataFusionRepository,
            GeoPositionValidatorRepository geoPositionValidatorRepository,
            ServerDataClientHandler serverDataClientHandler,
            GuidanceDataClientHandler guidanceDataClientHandler,
            DataBatchUtils<DataFusionTarget> dataBatchUtils, DataFusionTargetMapper dataFusionTargetMapper) {
        this.algorithmClient = algorithmClient;
        this.objectMapper = objectMapper;
        this.objectDetectionRepository = objectDetectionRepository;
        this.trackPredictionRepository = trackPredictionRepository;
        this.dataFusionRepository = dataFusionRepository;
        this.geoPositionValidatorRepository = geoPositionValidatorRepository;
        this.serverDataClientHandler = serverDataClientHandler;
        this.guidanceDataClientHandler = guidanceDataClientHandler;
        this.dataBatchUtils = dataBatchUtils;
        this.dataFusionTargetMapper = dataFusionTargetMapper;

        // 注册批量处理器
        dataBatchUtils.register(
                BUSINESS_KEY,
                this::batchSave,  // 保存回调方法
                1000,             // 最大批量大小
                5000              // 刷新间隔5秒
        );
    }

    @PostConstruct
    public void initializeStreams() {
        log.info("初始化gRPC流连接...");
        startFusionStream();
        // 根据需要启用其他流
        // startTrackStream();
        // startImageStream();
    }

    @PreDestroy
    public void cleanup() {
        log.info("关闭gRPC流连接...");
        shutdownAllStreams();
    }

    /**
     * 启动批量存储工作线程
     */
    private void startBatchStorageWorkers() {
        // 数据融合批量存储
        asyncStorageExecutor.submit(() -> {
            List<DataFusionEntity> batch = new ArrayList<>();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 等待数据或超时
                    DataFusionEntity entity = dataFusionQueue.poll(BATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    if (entity != null) {
                        batch.add(entity);
                    }

                    // 批量保存条件：达到批次大小或超时
                    if (batch.size() >= BATCH_SIZE || (!batch.isEmpty() && entity == null)) {
                        saveBatch(batch, dataFusionRepository::saveAll, "数据融合");
                        batch.clear();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("批量存储数据融合失败", e);
                }
            }
        });

        // 轨迹预测批量存储
        asyncStorageExecutor.submit(() -> {
            List<TrackPredictionEntity> batch = new ArrayList<>();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    TrackPredictionEntity entity = trackPredictionQueue.poll(BATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    if (entity != null) {
                        batch.add(entity);
                    }

                    if (batch.size() >= BATCH_SIZE || (!batch.isEmpty() && entity == null)) {
                        saveBatch(batch, trackPredictionRepository::saveAll, "轨迹预测");
                        batch.clear();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("批量存储轨迹预测失败", e);
                }
            }
        });

        // 目标检测批量存储
        asyncStorageExecutor.submit(() -> {
            List<ObjectDetectionEntity> batch = new ArrayList<>();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ObjectDetectionEntity entity = objectDetectionQueue.poll(BATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    if (entity != null) {
                        batch.add(entity);
                    }

                    if (batch.size() >= BATCH_SIZE || (!batch.isEmpty() && entity == null)) {
                        saveBatch(batch, objectDetectionRepository::saveAll, "目标检测");
                        batch.clear();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("批量存储目标检测失败", e);
                }
            }
        });

        // 地理位置验证批量存储
        asyncStorageExecutor.submit(() -> {
            List<GeoPositionValidatorEntity> batch = new ArrayList<>();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    GeoPositionValidatorEntity entity = geoValidatorQueue.poll(BATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    if (entity != null) {
                        batch.add(entity);
                    }

                    if (batch.size() >= BATCH_SIZE || (!batch.isEmpty() && entity == null)) {
                        saveBatch(batch, geoPositionValidatorRepository::saveAll, "地理位置验证");
                        batch.clear();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("批量存储地理位置验证失败", e);
                }
            }
        });

        log.info("批量存储工作线程已启动");
    }

    /**
     * 批量保存回调方法
     *
     * @param dataList
     */
    private void batchSave(List<DataFusionTarget> dataList) {
        try {
            // 使用MyBatis-Plus的saveBatch方法
            dataFusionTargetMapper.insert(dataList);
        } catch (Exception e) {
            log.error("批量保存异常", e);
            throw new RuntimeException("批量保存异常", e);
        }
    }

    /**
     * 通用批量保存方法
     */
    private <T> void saveBatch(List<T> batch, Consumer<List<T>> saveFunction, String dataType) {
        if (batch.isEmpty()) return;

        try {
            long startTime = System.currentTimeMillis();
            saveFunction.accept(batch);
            long duration = System.currentTimeMillis() - startTime;
            log.debug("批量保存{}条{}数据，耗时{}ms", batch.size(), dataType, duration);
        } catch (Exception e) {
            log.error("批量保存{}数据失败", dataType, e);
        }
    }

    /**
     * 关闭异步存储
     */
    private void shutdownAsyncStorage() {
        try {
            asyncStorageExecutor.shutdown();
            if (!asyncStorageExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncStorageExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncStorageExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 关闭所有流连接
     */
    private void shutdownAllStreams() {
        fusionStreamActive.set(false);
        trackStreamActive.set(false);
        imageStreamActive.set(false);

        // 清理SSE连接
        sseEmitters.clear();
    }

    /**
     * 启动融合数据流
     */
    public void startFusionStream() {
        if (fusionStreamActive.compareAndSet(false, true)) {
            log.info("启动融合数据流...");
            algorithmClient.subscribeFusionStream(createFusionStreamObserver());
        } else {
            log.warn("融合数据流已经在运行中");
        }
    }

    /**
     * 创建融合数据流观察者
     */
    private StreamObserver<Result> createFusionStreamObserver() {
        return new StreamObserver<Result>() {
            @Override
            public void onNext(Result value) {
                processFusionStreamData(value);
            }

            @Override
            public void onError(Throwable t) {
                log.error("融合数据流异常", t);
                fusionStreamActive.set(false);
                scheduleStreamReconnect("Fusion", () -> startFusionStream(), fusionReconnectCount);
            }

            @Override
            public void onCompleted() {
                log.info("融合数据流已完成");
                fusionStreamActive.set(false);
                scheduleStreamReconnect("Fusion", () -> startFusionStream(), fusionReconnectCount);
            }
        };
    }

    /**
     * 处理融合流数据
     */
    private void processFusionStreamData(Result value) {
        try {
            String json = value.getJsonData().toStringUtf8();
            String timestamp = value.getTimestamp();

            log.debug("接收到融合数据流: timestamp={}, dataSize={}", timestamp, json.length());
//            {
//                "target_seq": "20260421000193",
//                    "target_type": "\u878d\u5408",
//                    "uav_model": "DJI Mavic",
//                    "azimuth": 0.0,
//                    "pitch": 0.0,
//                    "range": 977.0,
//                    "target_lon": 114.071686,
//                    "target_lat": 22.433599,
//                    "altitude": 0.0,
//                    "speed": 0.0,
//                    "timestamp": 1776755488456,
//                    "radar_targetd_id": null,
//                    "tdoa_targetd_id": "3200502"
//            }
//            AlgorithmResult algorithmResult = objectMapper.readValue(json, AlgorithmResult.class);
//
//            // 确保数据不为null
//            if (algorithmResult.getDataFusion() == null) {
//                algorithmResult.setDataFusion(Collections.emptyList());
//            }
//            if (algorithmResult.getGeoPositionValidator() == null) {
//                algorithmResult.setGeoPositionValidator(Collections.emptyList());
//            }

            // 1. 立即更新内存数据（最高优先级）
//            updateDataStores(algorithmResult);

            // 2. 立即推送到SSE客户端（第二优先级）
//            pushFusionDataToSse(algorithmResult);

            // 3. 异步存储到数据库（最低优先级，不阻塞）
//            asyncStorageExecutor.submit(() -> {
//                try {
//                    saveDataToDatabase(algorithmResult);
//                } catch (Exception e) {
//                    log.error("异步存储数据失败", e);
//                }
//            });
            DataFusionResponse dataFusionResponse = objectMapper.readValue(json, DataFusionResponse.class);

            if (dataFusionResponse != null && !dataFusionResponse.getDataFusion().isEmpty()) {
                dataFusionResponse.getDataFusion().forEach(entity -> {

                    DataFusionTarget dataFusionEntity = DataFusionTarget.builder()
                            .id(UUID.randomUUID().toString())
                            .targetId(entity.getTargetSeq())
                            .targetBatch(System.currentTimeMillis())
                            .radarTargetId(entity.getRadarTargetdId())
                            .tdoaTargetId(entity.getTdoaTargetdId())
                            .timestamp(new Date())
                            .range(entity.getRange())
                            .azimuth(entity.getAzimuth())
                            .pitch(entity.getPitch())
                            .speed(entity.getSpeed())
                            .altitude(entity.getAltitude())
                            .targetLat(entity.getTargetLat())
                            .targetLon(entity.getTargetLon())
                            .targetType(entity.getTargetType())
                            .uavModel(entity.getUavModel())
                            .build();

                    threatAssessmentService.algorithmDataHandel(dataFusionEntity);
                    this.guidanceDataClientHandler.dataAlgorithmForward(dataFusionEntity);

                    dataBatchUtils.add(BUSINESS_KEY, dataFusionEntity);
                });
            }

            // 重置重连计数器
            fusionReconnectCount.set(0);

        } catch (JsonProcessingException e) {
            log.error("解析融合流数据JSON失败", e);
        } catch (Exception e) {
            log.error("处理融合流数据异常", e);
        }
    }

    /**
     * 启动轨迹预测数据流
     */
    public void startTrackStream() {
        if (trackStreamActive.compareAndSet(false, true)) {
            log.info("启动轨迹预测数据流...");
            algorithmClient.subscribeTrackStream(createTrackStreamObserver());
        } else {
            log.warn("轨迹预测数据流已经在运行中");
        }
    }

    /**
     * 创建轨迹预测数据流观察者
     */
    private StreamObserver<Result> createTrackStreamObserver() {
        return new StreamObserver<Result>() {
            @Override
            public void onNext(Result value) {
                processTrackStreamData(value);
            }

            @Override
            public void onError(Throwable t) {
                log.error("轨迹预测数据流异常", t);
                trackStreamActive.set(false);
                scheduleStreamReconnect("Track", () -> startTrackStream(), trackReconnectCount);
            }

            @Override
            public void onCompleted() {
                log.info("轨迹预测数据流已完成");
                trackStreamActive.set(false);
                scheduleStreamReconnect("Track", () -> startTrackStream(), trackReconnectCount);
            }
        };
    }

    /**
     * 处理轨迹预测流数据
     */
    private void processTrackStreamData(Result value) {
        try {
            String json = value.getJsonData().toStringUtf8();
            String timestamp = value.getTimestamp();

            log.debug("接收到轨迹预测数据流: timestamp={}, dataSize={}", timestamp, json.length());

            TrackPrediction trackPrediction = parseTrackPredictionWithWrapper(json);
            if (trackPrediction != null) {
                // 1. 立即更新内存数据
                updateTrackData(trackPrediction);

                // 2. 立即推送到SSE客户端
                pushToSseEmitters(trackPrediction);

                // 3. 异步存储到数据库
//                asyncStorageExecutor.submit(() -> {
//                    try {
//                        saveTrackDataAsync(trackPrediction);
//                    } catch (Exception e) {
//                        log.error("异步存储轨迹数据失败", e);
//                    }
//                });
            }

            // 重置重连计数器
            trackReconnectCount.set(0);

        } catch (Exception e) {
            log.error("处理轨迹预测流数据异常", e);
        }
    }

    /**
     * 启动图像数据流
     */
    public void startImageStream() {
        if (imageStreamActive.compareAndSet(false, true)) {
            log.info("启动图像数据流...");
            algorithmClient.subscribeImageStream(createImageStreamObserver());
        } else {
            log.warn("图像数据流已经在运行中");
        }
    }

    /**
     * 创建图像数据流观察者
     */
    private StreamObserver<Result> createImageStreamObserver() {
        return new StreamObserver<Result>() {
            @Override
            public void onNext(Result value) {
                processImageStreamData(value);
            }

            @Override
            public void onError(Throwable t) {
                log.error("图像数据流异常", t);
                imageStreamActive.set(false);
                scheduleStreamReconnect("Image", () -> startImageStream(), imageReconnectCount);
            }

            @Override
            public void onCompleted() {
                log.info("图像数据流已完成");
                imageStreamActive.set(false);
                scheduleStreamReconnect("Image", () -> startImageStream(), imageReconnectCount);
            }
        };
    }

    /**
     * 处理图像流数据
     */
    private void processImageStreamData(Result value) {
        try {
            String json = value.getJsonData().toStringUtf8();
            String timestamp = value.getTimestamp();

            log.debug("接收到图像数据流: timestamp={}, dataSize={}", timestamp, json.length());

            ObjectDetection detection = parseObjectDetection(json);
            if (detection != null && detection.getTargets() != null && !detection.getTargets().isEmpty()) {
                // 1. 立即更新内存数据
                currentDetection.set(detection);

                // 2. 立即推送到SSE客户端
                pushObjectDetectionToSse(detection);

                // 3. 异步存储到数据库
                asyncStorageExecutor.submit(() -> {
                    try {
                        saveObjectDetectionAsync(detection);
                    } catch (Exception e) {
                        log.error("异步存储目标检测数据失败", e);
                    }
                });

                log.info("更新目标检测数据，发现 {} 个目标", detection.getTargets().size());
            } else {
                log.debug("目标检测结果为空或无有效目标");
            }

            // 重置重连计数器
            imageReconnectCount.set(0);

        } catch (Exception e) {
            log.error("处理图像流数据异常", e);
        }
    }

    /**
     * 安排流重连任务
     */
    private void scheduleStreamReconnect(String streamName, Runnable reconnectTask, AtomicInteger reconnectCounter) {
        int count = reconnectCounter.incrementAndGet();
        if (count <= MAX_RECONNECT_ATTEMPTS) {
            log.warn("{}流将在{}秒后进行第{}次重连", streamName, RECONNECT_DELAY_MS / 1000, count);
            CompletableFuture.delayedExecutor(RECONNECT_DELAY_MS, TimeUnit.MILLISECONDS)
                    .execute(reconnectTask);
        } else {
            log.error("{}流重连次数已达上限({})，停止重连", streamName, MAX_RECONNECT_ATTEMPTS);
        }
    }

    /**
     * 异步存储轨迹数据
     */
    private void saveTrackDataAsync(TrackPrediction trackPrediction) {
        if (trackPrediction == null ||
                trackPrediction.getPredictions() == null ||
                trackPrediction.getPredictions().isEmpty()) {
            return;
        }

        int sequenceNum = 0;
        for (PredictionPoint point : trackPrediction.getPredictions()) {
            TrackPredictionEntity entity = new TrackPredictionEntity();
            entity.setTargetId(trackPrediction.getTargetId());
            entity.setLatitude(point.getLatitude());
            entity.setLongitude(point.getLongitude());
            entity.setAltitude(point.getAltitude());
            entity.setSequenceNumber(sequenceNum++);

            // 加入队列等待批量存储
            if (!trackPredictionQueue.offer(entity)) {
                log.warn("轨迹预测数据队列已满，丢弃数据");
            }
        }
    }

    /**
     * 异步存储目标检测数据
     */
    private void saveObjectDetectionAsync(ObjectDetection detection) {
        if (detection == null ||
                detection.getTargets() == null ||
                detection.getTargets().isEmpty()) {
            return;
        }

        for (DetectionTarget target : detection.getTargets()) {
            ObjectDetectionEntity entity = new ObjectDetectionEntity();
            // 根据实际的ObjectDetectionEntity字段设置数据
            // entity.setTargetId(target.getId());
            // entity.setConfidence(target.getConfidence());
            // ... 其他字段设置

            // 加入队列等待批量存储
            if (!objectDetectionQueue.offer(entity)) {
                log.warn("目标检测数据队列已满，丢弃数据");
            }
        }
    }

    /**
     * 异步存储数据到数据库
     */
//    private void saveDataToDatabase(AlgorithmResult result) {
//        // 保存数据融合结果
//        if (result.getDataFusion() != null && !result.getDataFusion().isEmpty()) {
//            for (DataFusion fusion : result.getDataFusion()) {
//                DataFusionEntity entity = new DataFusionEntity();
//                entity.setTargetId(fusion.getId());
//
//                // 检查fusion.getPosition()是否为非空数组且长度至少为3
//                double[] position = fusion.getPosition();
//                if (position != null && position.length >= 3) {
//                    entity.setLatitude(position[0]);
//                    entity.setLongitude(position[1]);
//                    entity.setAltitude(position[2]);
//                } else {
//                    log.warn("Invalid position array for data fusion. TargetId: {}", fusion.getId());
//                    continue; // 跳过无效数据
//                }
//
//                // 检查目标ID是否已经观察过
//                if (!observedTargetIds.contains(fusion.getId())) {
//                    // 监控无人机数量统计
//                    droneStatsService.droneCount();
//                    // 非法无人机数量统计
//                    if (fusion.getThreadLevel() == 3) {
//                        droneStatsService.incrementIllegalDroneCount();
//                    }
//                    observedTargetIds.add((long) fusion.getId());
//                }
//
//                entity.setVelocity(fusion.getVelocity());
//                entity.setAzimuth(fusion.getAzimuth());
//                entity.setType(fusion.getType());
//                entity.setName(fusion.getName());
//                entity.setDistance(fusion.getDistance());
//                entity.setPitch(fusion.getPitch());
//                entity.setThreatLevel(String.valueOf(fusion.getThreadLevel()));
//                entity.setPanAngle(fusion.getPanAngle());
//                entity.setTiltAngle(fusion.getTiltAngle());
//                entity.setZoomLevel(fusion.getZoomLevel());
//                entity.setColor(fusion.getColor());
//
//                dataFusionRepository.save(entity);
//                log.info("已保存数据融合结果到数据库，目标ID: {}", fusion.getId());
//                // 加入队列等待批量存储
//                if (!dataFusionQueue.offer(entity)) {
//                    log.warn("数据融合队列已满，丢弃数据");
//                }
//
//                // 统计信息立即更新（内存操作）
//                updateDroneStats(fusion);
//            }
//        }
//
//        // 保存地理位置验证结果
//        if (result.getGeoPositionValidator() != null && !result.getGeoPositionValidator().isEmpty()) {
//            GeoPositionValidator validator = result.getGeoPositionValidator().get(0);
//            GeoPositionValidatorEntity entity = new GeoPositionValidatorEntity();
//
//            // 检查validator.getPosition()是否为非空数组且长度至少为3
//            double[] position = validator.getPosition();
//            if (position != null && position.length >= 3) {
//                entity.setLongitude(position[0]);
//                entity.setLatitude(position[1]);
//                entity.setAltitude(position[2]);
//            } else {
//                log.warn("Invalid position array for geo position validator");
//                // 可选择设置默认值或抛出异常
//            }
//
//            entity.setWarningLevel(validator.getWarningLevel());
//
//            geoPositionValidatorRepository.save(entity);
//            log.info("已保存地理位置验证结果到数据库，警告级别: {}", validator.getWarningLevel());
//            // 加入队列等待批量存储
//            if (!geoValidatorQueue.offer(entity)) {
//                log.warn("地理位置验证队列已满，丢弃数据");
//            }
//        }
//    }

    /**
     * 手动重启所有流
     */
    public void restartAllStreams() {
        log.info("手动重启所有gRPC流...");

        // 关闭现有流
        shutdownAllStreams();

        // 重置重连计数器
        resetReconnectCounters();

        // 延迟重启
        CompletableFuture.delayedExecutor(STREAM_RESTART_DELAY_MS, TimeUnit.MILLISECONDS)
                .execute(() -> {
                    startFusionStream();
                    startTrackStream();
                    startImageStream();
                });
    }

    /**
     * 重置重连计数器
     */
    private void resetReconnectCounters() {
        fusionReconnectCount.set(0);
        trackReconnectCount.set(0);
        imageReconnectCount.set(0);
    }

    /**
     * 获取流状态
     */
    public Map<String, Object> getStreamStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("fusionStreamActive", fusionStreamActive.get());
        status.put("trackStreamActive", trackStreamActive.get());
        status.put("imageStreamActive", imageStreamActive.get());
        status.put("fusionReconnectCount", fusionReconnectCount.get());
        status.put("trackReconnectCount", trackReconnectCount.get());
        status.put("imageReconnectCount", imageReconnectCount.get());
        status.put("sseConnectionCount", sseEmitters.size());
        return status;
    }

    /**
     * 轨迹预测包装类
     */
    @Getter
    private static class TrackPredictionWrapper {
        @JsonProperty("TrackPrediction")
        private List<TrackPrediction> trackPredictions;
    }

    /**
     * 解析带包装的轨迹预测数据
     */
    private TrackPrediction parseTrackPredictionWithWrapper(String jsonData) {
        try {
            TrackPredictionWrapper wrapper = objectMapper.readValue(jsonData, TrackPredictionWrapper.class);

            if (wrapper.getTrackPredictions() != null && !wrapper.getTrackPredictions().isEmpty()) {
                return wrapper.getTrackPredictions().get(0);
            }

            return createEmptyTrackPrediction();

        } catch (JsonProcessingException e) {
            log.error("解析轨迹预测JSON失败: {}", jsonData, e);
            return createEmptyTrackPrediction();
        }
    }

    /**
     * 创建空的轨迹预测对象
     */
    private TrackPrediction createEmptyTrackPrediction() {
        TrackPrediction emptyPrediction = new TrackPrediction();
        emptyPrediction.setTargetId(0L);
        emptyPrediction.setPredictions(new ArrayList<>());
        return emptyPrediction;
    }

    /**
     * 解析目标检测数据
     */
    private ObjectDetection parseObjectDetection(String jsonData) {
        try {
            ObjectDetectionWrapper wrapper = objectMapper.readValue(jsonData, ObjectDetectionWrapper.class);

            if (wrapper.getObjectDetection() != null && !wrapper.getObjectDetection().isEmpty()) {
                ObjectDetection result = wrapper.getObjectDetection().get(0);

                // 验证目标数据
                if (result.getTargets() != null && !result.getTargets().isEmpty()) {
                    return result;
                }
            }

            log.debug("目标检测结果中无有效目标");
            return null;

        } catch (JsonProcessingException e) {
            log.error("解析目标检测数据失败: {}", jsonData, e);
            return null;
        }
    }

    /**
     * 目标检测包装类
     */
    @Getter
    private static class ObjectDetectionWrapper {
        @JsonProperty("ObjectDetection")
        private List<ObjectDetection> objectDetection;
    }

    /**
     * 将轨迹预测数据推送到SSE客户端
     */
    public void pushToSseEmitters(TrackPrediction trackPrediction) {
        if (trackPrediction != null &&
                trackPrediction.getPredictions() != null &&
                !trackPrediction.getPredictions().isEmpty()) {

            Map<String, Object> sseData = new HashMap<>();
            sseData.put("type", "track_prediction");
            sseData.put("targetId", trackPrediction.getTargetId());
            sseData.put("predictions", formatPoints(trackPrediction.getPredictions()));
            sseData.put("timestamp", System.currentTimeMillis());

            sendToSseClients(sseData);
        }
    }

//    /**
//     * 将融合数据推送到SSE客户端
//     */
//    private void pushFusionDataToSse(AlgorithmResult algorithmResult) {
//        if (algorithmResult.getDataFusion() != null && !algorithmResult.getDataFusion().isEmpty()) {
//            Map<String, Object> sseData = new HashMap<>();
//            sseData.put("type", "data_fusion");
//            sseData.put("data", getDroneVOList());
//            sseData.put("timestamp", System.currentTimeMillis());
//            sendToSseClients(sseData);
//        }
//
//        if (algorithmResult.getGeoPositionValidator() != null && !algorithmResult.getGeoPositionValidator().isEmpty()) {
//            Map<String, Object> sseData = new HashMap<>();
//            sseData.put("type", "geo_validation");
//            sseData.put("data", getThreatDetail());
//            sseData.put("timestamp", System.currentTimeMillis());
//            sendToSseClients(sseData);
//        }
//    }

    /**
     * 将目标检测数据推送到SSE客户端
     */
    private void pushObjectDetectionToSse(ObjectDetection detection) {
        if (detection != null && detection.getTargets() != null && !detection.getTargets().isEmpty()) {
            Map<String, Object> sseData = new HashMap<>();
            sseData.put("type", "object_detection");
            sseData.put("targets", detection.getTargets());
            sseData.put("timestamp", System.currentTimeMillis());
            sendToSseClients(sseData);
        }
    }

    /**
     * 统一的SSE数据发送方法
     */
    private void sendToSseClients(Map<String, Object> data) {
        if (sseEmitters.isEmpty()) {
            return;
        }

        Iterator<SseEmitter> iterator = sseEmitters.iterator();
        while (iterator.hasNext()) {
            SseEmitter emitter = iterator.next();
            try {
                emitter.send(SseEmitter.event()
                        .data(data)
                        .id(UUID.randomUUID().toString()));
            } catch (IOException e) {
                log.warn("SSE数据发送失败，移除失败的连接", e);
                iterator.remove();
            }
        }
    }

    /**
     * 格式化轨迹点
     */
    public List<Map<String, Double>> formatPoints(List<PredictionPoint> points) {
        return Optional.ofNullable(points)
                .orElse(Collections.emptyList())
                .stream()
                .map(p -> {
                    Map<String, Double> point = new HashMap<>();
                    point.put("lng", p.getLongitude());
                    point.put("lat", p.getLatitude());
                    point.put("alt", p.getAltitude());
                    return point;
                })
                .collect(Collectors.toList());
    }

//    /**
//     * 更新数据存储
//     */
//    private void updateDataStores(AlgorithmResult result) {
//
//        if (result.getDataFusion() != null && !result.getDataFusion().isEmpty()) {
//            currentFusions.set(new ArrayList<>(result.getDataFusion()));
//
//
//            List<DroneVO> droneVOList = getDroneVOList();
//            threatAssessmentService.algorithmDataHandel(droneVOList);
//            this.guidanceDataClientHandler.dataForward(droneVOList);
//        }
//
//        if (result.getGeoPositionValidator() != null && !result.getGeoPositionValidator().isEmpty()) {
//            GeoPositionValidator geoPositionValidator = result.getGeoPositionValidator().get(0);
//            currentGeoValidator.set(geoPositionValidator);
//
//            log.info("===============收到算法数据 start========================");
//            log.info("%s".formatted(geoPositionValidator));
//            log.info("===============收到算法数据 end  ========================");
//            List<DroneVO> droneVOList = getDroneVOList();
//
//            // 发送给前端
//            JSONObject data = new JSONObject();
//            data.put("target", droneVOList);
//            data.put("threatLevel", 0);
//            this.serverDataClientHandler.broadcast(Constant.DATA_FUSIONS, data);
//            this.guidanceDataClientHandler.dataForward(droneVOList);
//        }
//    }

    /**
     * 更新轨迹数据
     */
    private void updateTrackData(TrackPrediction trackPrediction) {
        if (trackPrediction != null) {
            List<TrackPrediction> predictions = new ArrayList<>();
            predictions.add(trackPrediction);
            currentPredictions.set(predictions);
            log.debug("更新目标ID[{}]的轨迹数据", trackPrediction.getTargetId());
        }
    }

//    /**
//     * 保存数据到数据库
//     */
//    private void saveDataToDatabase(AlgorithmResult result) {
//        // 保存所有数据融合结果
//        if (result.getDataFusion() != null && !result.getDataFusion().isEmpty()) {
//            for (DataFusion fusion : result.getDataFusion()) {
//                DataFusionEntity entity = new DataFusionEntity();
//                entity.setTargetId(fusion.getId());
//
//                // 检查fusion.getPosition()是否为非空数组且长度至少为3
//                double[] position = fusion.getPosition();
//                if (position != null && position.length >= 3) {
//                    entity.setLatitude(position[0]);
//                    entity.setLongitude(position[1]);
//                    entity.setAltitude(position[2]);
//                } else {
//                    log.warn("Invalid position array for data fusion. TargetId: {}", fusion.getId());
//                    continue; // 跳过无效数据
//                }
//
//                // 检查目标ID是否已经观察过
//                if (!observedTargetIds.contains(fusion.getId())) {
//                    // 监控无人机数量统计
//                    droneStatsService.droneCount();
//                    // 非法无人机数量统计
//                    if (fusion.getThreadLevel() == 3) {
//                        droneStatsService.incrementIllegalDroneCount();
//                    }
//                    observedTargetIds.add((long) fusion.getId());
//                }
//
//                entity.setVelocity(fusion.getVelocity());
//                entity.setAzimuth(fusion.getAzimuth());
//                entity.setType(fusion.getType());
//                entity.setName(fusion.getName());
//                entity.setDistance(fusion.getDistance());
//                entity.setPitch(fusion.getPitch());
//                entity.setThreatLevel(String.valueOf(fusion.getThreadLevel()));
//                entity.setPanAngle(fusion.getPanAngle());
//                entity.setTiltAngle(fusion.getTiltAngle());
//                entity.setZoomLevel(fusion.getZoomLevel());
//                entity.setColor(fusion.getColor());
//
//                dataFusionRepository.save(entity);
//                log.info("已保存数据融合结果到数据库，目标ID: {}", fusion.getId());
//            }
//        }
//
//        // 保存地理位置验证结果
//        if (!result.getGeoPositionValidator().isEmpty()) {
//            GeoPositionValidator validator = result.getGeoPositionValidator().get(0);
//            GeoPositionValidatorEntity entity = new GeoPositionValidatorEntity();
//
//            // 检查validator.getPosition()是否为非空数组且长度至少为3
//            double[] position = validator.getPosition();
//            if (position != null && position.length >= 3) {
//                entity.setLongitude(position[0]);
//                entity.setLatitude(position[1]);
//                entity.setAltitude(position[2]);
//            } else {
//                log.warn("Invalid position array for geo position validator");
//                // 可选择设置默认值或抛出异常
//            }
//
//            entity.setWarningLevel(validator.getWarningLevel());
//
//            geoPositionValidatorRepository.save(entity);
//            log.info("已保存地理位置验证结果到数据库，警告级别: {}", validator.getWarningLevel());
//        }
//    }
//
//    /**
//     * 保存数据融合结果
//     */
//    private void saveDataFusionResults(List<DataFusion> dataFusions) {
//        if (dataFusions == null || dataFusions.isEmpty()) {
//            return;
//        }
//
//        for (DataFusion fusion : dataFusions) {
//            try {
//                DataFusionEntity entity = createDataFusionEntity(fusion);
//                if (entity != null) {
//                    dataFusionRepository.save(entity);
//                    updateDroneStats(fusion);
//                    log.debug("保存数据融合结果，目标ID: {}", fusion.getId());
//                }
//            } catch (Exception e) {
//                log.error("保存数据融合结果失败，目标ID: {}", fusion.getId(), e);
//            }
//        }
//    }

//    /**
//     * 创建数据融合实体
//     */
//    private DataFusionEntity createDataFusionEntity(DataFusion fusion) {
//        double[] position = fusion.getPosition();
//        if (position == null || position.length < 3) {
//            log.warn("数据融合位置数组无效，目标ID: {}", fusion.getId());
//            return null;
//        }
//
//        DataFusionEntity entity = new DataFusionEntity();
//        entity.setTargetId(fusion.getId());
//        entity.setLatitude(position[0]);
//        entity.setLongitude(position[1]);
//        entity.setAltitude(position[2]);
//        entity.setVelocity(fusion.getVelocity());
//        entity.setAzimuth(fusion.getAzimuth());
//        entity.setType(fusion.getType());
//        entity.setName(fusion.getName());
//        entity.setDistance(fusion.getDistance());
//        entity.setPitch(fusion.getPitch());
//        entity.setThreatLevel(String.valueOf(fusion.getThreadLevel()));
//        entity.setPanAngle(fusion.getPanAngle());
//        entity.setTiltAngle(fusion.getTiltAngle());
//        entity.setZoomLevel(fusion.getZoomLevel());
//        entity.setColor(fusion.getColor());
//
//        return entity;
//    }

//    /**
//     * 更新无人机统计
//     */
//    private void updateDroneStats(DataFusion fusion) {
//        if (observedTargetIds.add((long) fusion.getId())) {
//            droneStatsService.droneCount();
//            if (fusion.getThreadLevel() == 3) {
//                droneStatsService.incrementIllegalDroneCount();
//            }
//        }
//    }

    /**
     * 保存地理位置验证结果
     */
    private void saveGeoValidationResults(List<GeoPositionValidator> validators) {
        if (validators == null || validators.isEmpty()) {
            return;
        }

        try {
            GeoPositionValidator validator = validators.get(0);
            GeoPositionValidatorEntity entity = createGeoValidationEntity(validator);
            if (entity != null) {
                geoPositionValidatorRepository.save(entity);
                log.debug("保存地理位置验证结果，警告级别: {}", validator.getWarningLevel());
            }
        } catch (Exception e) {
            log.error("保存地理位置验证结果失败", e);
        }
    }

    /**
     * 创建地理位置验证实体
     */
    private GeoPositionValidatorEntity createGeoValidationEntity(GeoPositionValidator validator) {
        double[] position = validator.getPosition();
        if (position == null || position.length < 3) {
            log.warn("地理位置验证位置数组无效");
            return null;
        }

        GeoPositionValidatorEntity entity = new GeoPositionValidatorEntity();
        entity.setLongitude(position[0]);
        entity.setLatitude(position[1]);
        entity.setAltitude(position[2]);
        entity.setWarningLevel(validator.getWarningLevel());

        return entity;
    }

    /**
     * 保存轨迹数据
     */
    public void saveTrackData(TrackPrediction trackPrediction) {
        if (trackPrediction == null ||
                trackPrediction.getPredictions() == null ||
                trackPrediction.getPredictions().isEmpty()) {
            return;
        }

        try {
            int sequenceNum = 0;
            for (PredictionPoint point : trackPrediction.getPredictions()) {
                TrackPredictionEntity entity = new TrackPredictionEntity();
                entity.setTargetId(trackPrediction.getTargetId());
                entity.setLatitude(point.getLatitude());
                entity.setLongitude(point.getLongitude());
                entity.setAltitude(point.getAltitude());
                entity.setSequenceNumber(sequenceNum++);
                trackPredictionRepository.save(entity);
            }
            log.debug("保存目标ID[{}]的{}个轨迹预测点",
                    trackPrediction.getTargetId(), trackPrediction.getPredictions().size());
        } catch (Exception e) {
            log.error("保存轨迹预测数据失败，目标ID: {}", trackPrediction.getTargetId(), e);
        }
    }

    /**
     * 保存目标检测数据
     */
    private void saveObjectDetection(ObjectDetection detection) {
        if (detection == null ||
                detection.getTargets() == null ||
                detection.getTargets().isEmpty()) {
            return;
        }

        try {
            for (DetectionTarget target : detection.getTargets()) {
                ObjectDetectionEntity entity = new ObjectDetectionEntity();
                // 根据实际的ObjectDetectionEntity字段设置数据
                // entity.setTargetId(target.getId());
                // entity.setConfidence(target.getConfidence());
                // ... 其他字段设置
                objectDetectionRepository.save(entity);
            }
            log.debug("保存{}个目标检测结果", detection.getTargets().size());
        } catch (Exception e) {
            log.error("保存目标检测数据失败", e);
        }
    }

    // ==================== 数据访问接口 ====================

    /**
     * 获取当前目标检测结果
     */
    public ObjectDetection getCurrentDetection() {
        return currentDetection.get();
    }

    /**
     * 从数据库获取最新的目标检测结果
     */
    public List<ObjectDetectionEntity> getLatestDetections(int limit) {
        return objectDetectionRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 根据目标ID获取预测轨迹点列表
     */
    public List<PredictionPoint> getPredictionPointsByTargetId(long targetId) {
        List<TrackPredictionEntity> entities = trackPredictionRepository.findByTargetIdOrderBySequenceNumber(targetId);
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(entity -> PredictionPoint.builder()
                        .latitude(entity.getLatitude())
                        .longitude(entity.getLongitude())
                        .altitude(entity.getAltitude())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 从数据库获取指定目标的轨迹预测
     */
    public List<TrackPredictionEntity> getTrackPredictionsByTargetId(Long targetId) {
        return trackPredictionRepository.findByTargetIdOrderBySequenceNumber(targetId);
    }

    /**
     * 获取所有目标的所有预测点，按目标ID分组
     */
    public Map<Long, List<PredictionPoint>> getAllPredictionPointsGroupedByTarget() {
        List<TrackPredictionEntity> entities = trackPredictionRepository.findAll();
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyMap();
        }
        return entities.stream()
                .collect(Collectors.groupingBy(
                        TrackPredictionEntity::getTargetId,
                        Collectors.mapping(entity -> PredictionPoint.builder()
                                        .latitude(entity.getLatitude())
                                        .longitude(entity.getLongitude())
                                        .altitude(entity.getAltitude())
                                        .build(),
                                Collectors.toList())
                ));
    }

    /**
     * 获取所有目标的预测点列表（为兼容现有代码）
     * 注意：如果有多个目标，将返回第一个目标的预测点
     */
    public List<PredictionPoint> getPredictionPoints() {
        List<TrackPredictionEntity> entities = trackPredictionRepository.findAll();
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        // 返回第一个轨迹的预测点（兼容原来的逻辑）
        return entities.stream()
                .filter(entity -> entity.getSequenceNumber() == 0)
                .map(entity -> PredictionPoint.builder()
                        .latitude(entity.getLatitude())
                        .longitude(entity.getLongitude())
                        .altitude(entity.getAltitude())
                        .build())
                .collect(Collectors.toList());
    }

    //--------------------- 数据融合接口 ---------------------

    // 获取所有数据
    public DataFusionVo getCurrentFusion() {
        List<DataFusionVo> fusions = currentFusions.get();
        return (fusions != null && !fusions.isEmpty()) ? fusions.get(0) : null;
    }

//    // 获取所有DataFusion
//    public List<DataFusion> getAllCurrentFusions() {
//        List<DataFusion> fusions = currentFusions.get();
//        return fusions != null ? new ArrayList<>(fusions) : Collections.emptyList();
//    }
//
//    // 根据targetId获取特定的DataFusion
//    public DataFusion getCurrentFusionByTargetId(int targetId) {
//        List<DataFusion> fusions = currentFusions.get();
//        if (fusions != null) {
//            return fusions.stream()
//                    .filter(fusion -> fusion.getId() == targetId)
//                    .findFirst()
//                    .orElse(null);
//        }
//        return null;
//    }

//    // 将DataFusion转换为DroneVO列表
//    public List<DroneVO> getDroneVOList() {
//        List<DataFusion> fusions = currentFusions.get();
//        if (fusions == null || fusions.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        List<DroneVO> drones = new ArrayList<>();
//        for (DataFusion fusion : fusions) {
//            // 检查getPosition()是否返回非空数组且长度至少为3
//            double[] position = fusion.getPosition();
//            if (position == null || position.length < 3) {
//                log.warn("Position array is null or length less than 3 for targetId: {}", fusion.getId());
//                continue; // 跳过无效数据
//            }
//
//            drones.add(DroneVO.builder()
//                    .id(fusion.getId())
//                    .latitude(position[0]) // 纬度
//                    .longitude(position[1]) // 经度
//                    .altitude(position[2]) // 高度
//                    .velocity(fusion.getVelocity())
//                    .azimuth(fusion.getAzimuth())
//                    .pitch(fusion.getPitch())
//                    .type(fusion.getType())
//                    .name(fusion.getName())
//                    .color(fusion.getColor())
//                    .threatLevel(fusion.getThreadLevel())
//                    .distance(fusion.getDistance())
//                    .panAngle(fusion.getPanAngle())
//                    .tiltAngle(fusion.getTiltAngle())
//                    .zoomLevel(fusion.getZoomLevel())
//                    .createTime(LocalDateTime.now())
//                    .lastUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
//                    .build());
//        }
//        return drones;
//    }

    //--------------------- 空间关系接口 ---------------------

    // 获取所有数据
    public GeoPositionValidator getCurrentGeoValidation() {
        return currentGeoValidator.get();
    }

    /**
     * 从数据库获取最新的地理位置验证结果
     */
    public GeoPositionValidatorEntity getLatestGeoValidation() {
        return geoPositionValidatorRepository.findTopByOrderByCreatedAtDesc();
    }

    // 获取告警级别
    public int getWarningLevel() {
        GeoPositionValidator validator = currentGeoValidator.get();
        return validator != null ? validator.getWarningLevel() : 0;
    }

    /**
     * 获取当前威胁状态
     *
     * @return "发生威胁" 或 空字符串
     */
    public String getThreatStatus() {
        GeoPositionValidator validator = currentGeoValidator.get();
        if (validator != null) {
            int level = validator.getWarningLevel();
            if (level >= 1 && level <= 3) {
                return "发生威胁";
            }
        }
        return "";
    }

    /**
     * 获取详细威胁信息（扩展接口）
     */
    public Map<String, Object> getThreatDetail() {
        GeoPositionValidator validator = currentGeoValidator.get();
        if (validator != null && validator.getWarningLevel() >= 1) {
            return Map.of(
                    "alert", "发生威胁",
                    "warningLevel", validator.getWarningLevel(),
                    "position", validator.getPosition(),
                    "timestamp", System.currentTimeMillis()
            );
        }
        return Collections.emptyMap();
    }
}
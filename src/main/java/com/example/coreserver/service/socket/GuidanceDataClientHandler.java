package com.example.coreserver.service.socket;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.example.coreserver.Constant;
import com.example.coreserver.entity.Config;
import com.example.coreserver.entity.DataFusionTarget;
import com.example.coreserver.entity.DataRadarTarget;
import com.example.coreserver.entity.DataTdoaTarget;
import com.example.coreserver.grpc.photoelectric.GuidanceStopParams;
import com.example.coreserver.grpc.photoelectric.RadarGuidanceParameters;
import com.example.coreserver.service.business.AzimuthElevationCalculatorNew;
import com.example.coreserver.service.device.ConfigService;
import com.example.coreserver.service.device.PhotoelectricService;
import com.example.coreserver.service.device.RadarService;
import com.example.coreserver.utils.ConfigUtils;
import com.example.coreserver.vo.GuidanceControlVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


@Slf4j
@Component
public class GuidanceDataClientHandler extends AbstractWebSocketHandler {

    private final RadarService radarService;
    private final ServerDataClientHandler serverDataClientHandler;
    private AzimuthElevationCalculatorNew azimuthElevationCalculator;
    Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    Map<String, GuidanceControlVo> dataMap = new ConcurrentHashMap<>();
    private final ConfigService configService;

    private final PhotoelectricService service;
    private int videoRecordingDuration;


    private int targetDistanceCorrection;
    private double targetAzimuthCorrection;
    private double targetElevationCorrection;
    private double northAngleVal;

    private double currentAzimuth;
    private double currentElevation;

    private AtomicBoolean isGuidance = new AtomicBoolean(false);

    // 自动模式 下的运行状态
    private AtomicBoolean autoTaskRunning = new AtomicBoolean(false);
    private GuidanceControlVo cacheGuidanceControlVo = null;
    // 雷达最后一次的数据
    private Map<String, Long> radarLastTime = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public GuidanceDataClientHandler(RadarService radarService, PhotoelectricService service, ConfigService configService, ServerDataClientHandler serverDataClientHandler) {
        this.radarService = radarService;
        this.service = service;
        this.configService = configService;
        this.serverDataClientHandler = serverDataClientHandler;
        init();

        scheduler.scheduleAtFixedRate(this::init, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());

        sessions.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        processMessage(session, payload);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        // 将二进制消息转换为字符串
        byte[] bytes = message.getPayload().array();
        String payload = new String(bytes, StandardCharsets.UTF_8);
        processMessage(session, payload);
    }

    private void processMessage(WebSocketSession session, String message) {
        try {
            // 处理业务逻辑
            // {"longitude": 114.42920687433397, "latitude": 22.701656848006774, "altitude": 34.61136068344116, "tarid": "97", "device_id": "RADAR01", "target_distance": 210}
            log.info("Processing message: {}", message);
            com.alibaba.fastjson2.JSONObject jsonObject = JSON.parseObject(message);
            Double longitude = jsonObject.getDouble("longitude");
            Double latitude = jsonObject.getDouble("latitude");
            Double altitude = jsonObject.getDouble("altitude");
            String deviceId = jsonObject.getString("device_id");
            int targetDistance = jsonObject.getInteger("altitude");
            int tarid = jsonObject.getInteger("tarid");
            String batchId = jsonObject.getString("batch_id");
//            String dataType = jsonObject.getString("data_type");
            String dataType = getCurrentDataType();

            double[] doubles = this.azimuthElevationCalculator.calculateAzEl(latitude, longitude, altitude);

            double azimuth = doubles[0];
            double elevation = doubles[1];
            RadarGuidanceParameters.Builder builder = RadarGuidanceParameters.newBuilder();

            GuidanceControlVo guidanceControlVo = getGuidanceControlVo();
            // 设置关键参数
            builder.setDeviceId("PE03")                    // 引导设备
                    .setTargetDistance(targetDistance + this.targetDistanceCorrection)        // 距离
                    // 方位角
                    .setTargetAzimuth((float) azimuth + this.targetAzimuthCorrection + (360 - this.northAngleVal))  // 方位角（需转换为float）
                    // elevation
                    .setTargetElevation((float) elevation - this.targetElevationCorrection)
                    .setVideoRecordingDuration(this.videoRecordingDuration)
                    .setDataType(dataType)
                    .setBatchId(StringUtils.isEmpty(batchId) ? "empty" : batchId)
                    .setTargetId(tarid); // 目标id

            this.currentAzimuth = azimuth;
            this.currentElevation = elevation;

            service.setRadarGuidanceMode(builder.build());

            // 发送前端的数据
            if (!isGuidance.get()) {
                isGuidance.set(true);
                // 开始引导，开始录制
                JSONObject data = new JSONObject();
                data.set("targetId", tarid);
                data.set("dataType", dataType);
                serverDataClientHandler.broadcast("GuidanceStart", data.toJSONString(0));
            }
            // 点位信息
            JSONObject data = new JSONObject();
            data.set("targetId", tarid);
            data.set("dataType", dataType);
            data.set("longitude", longitude);
            data.set("latitude", latitude);
            data.set("targetDistance", targetDistance);
            serverDataClientHandler.broadcast("GuidancePoint", data);

        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.warn("Connection closed: {} | Reason: {}", session.getId(), status.getReason());
        sessions.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket error on session {}: {}", session.getId(), exception.getMessage(), exception);
        sessions.remove(session.getId());
    }

    /**
     * 广播消息给所有连接的客户端（JSONObject 格式）
     *
     * @param jsonObject 数据
     */
    public void broadcast(JSONObject jsonObject) {

        WebSocketMessage<String> message = new TextMessage(jsonObject.toJSONString(0));
        sessions.values().forEach(session -> {
            executor.submit(() -> {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    log.error("Failed to send message: {}", message, e);
                    log.debug("broadcast called by thread: {}", Thread.currentThread().getName());
                }
            });

        });
    }

    /**
     * 前端推送的数据
     *
     * @param guidanceControlVo
     */
    public void guidanceControl(GuidanceControlVo guidanceControlVo) {
        if (Objects.equals(guidanceControlVo.getDataType(), "GuidanceStop")) {
            String targetId = getCurrentTargetId();
            String dataType = getCurrentDataType();

            // 点击了停止录制按钮，清空数据
            dataMap.clear();
            // 给采集端发送停止录制指令
            service.stopGuidance(GuidanceStopParams.newBuilder()
                    .setDeviceId("PE03")
                    .setTargetId(targetId)
                    .setSec(0)
                    .build());

            removeGuidance(dataType, targetId);
            return;
        }
        // 先清空在添加，保证只有一个目标在跟踪
        dataMap.clear();
        dataMap.put(guidanceControlVo.getDataType(), guidanceControlVo);
    }


    /**
     * 引导完成后，删除当前流程
     */
//    public void removeGuidance(PhotoelectricFileRecord photoelectricFileRecord) {
//        String dataType = photoelectricFileRecord.getTargetType();
//
//        if (dataType != null) {
//            dataMap.remove(dataType);
//
//            // 开始引导，开始录制
//            JSONObject data = new JSONObject();
//            data.set("targetId", photoelectricFileRecord.getTargetId());
//            data.set("dataType", dataType);
//            serverDataClientHandler.broadcast("GuidanceStop", data.toJSONString(0));
//
//        }
//    }
    public void removeGuidance(String dataType, String targetId) {
        isGuidance.set(false);
        // 开始引导，开始录制
        JSONObject data = new JSONObject();
        data.set("targetId", targetId);
        data.set("dataType", dataType);
        serverDataClientHandler.broadcast("GuidanceStop", data.toJSONString(0));
    }


    /**
     * 这是数据转发来的数据
     *
     */
    public void dataTDOAForward(DataTdoaTarget dataTdoaTarget) {
        CompletableFuture.runAsync(() -> {
            GuidanceControlVo guidanceControlVo = dataMap.get(Constant.DEVICE_TDOA);
            if (guidanceControlVo == null) {
                return;
            }

            if (Objects.equals(dataTdoaTarget.getSensorId(), guidanceControlVo.getTargetNo())) {

                JSONObject jsonObject = getJsonObject(dataTdoaTarget.getSensorId(),
                        dataTdoaTarget.getDeviceId(),
                        dataTdoaTarget.getUavDistance(),
                        dataTdoaTarget.getUavLat(),
                        dataTdoaTarget.getUavLon(),
                        dataTdoaTarget.getUavAlt(), Constant.DEVICE_TDOA,
                        this.currentAzimuth,
                        this.currentElevation,
                        dataTdoaTarget.getTraceId());
                this.broadcast(jsonObject);
            }

        });
    }

    public void dataAlgorithmForward(DataFusionTarget dataFusionEntity) {
        CompletableFuture.runAsync(() -> {
            if (algorithmAutomaticallyGuided(dataFusionEntity)) return;


            GuidanceControlVo guidanceControlVo = dataMap.get(Constant.DEVICE_FUSIONS);
            if (guidanceControlVo == null) {
                return;
            }
            if (Objects.equals(dataFusionEntity.getTargetId(), guidanceControlVo.getTargetNo())) {
                JSONObject jsonObject = getJsonObject(dataFusionEntity.getTargetId(),
                        "",
                        dataFusionEntity.getRange(),
                        dataFusionEntity.getTargetLat(),
                        dataFusionEntity.getTargetLon(),
                        dataFusionEntity.getAltitude(),
                        Constant.DEVICE_FUSIONS,
                        this.currentAzimuth,
                        this.currentElevation,
                        dataFusionEntity.getId());
                this.broadcast(jsonObject);
            }

        });
    }

    private boolean algorithmAutomaticallyGuided(DataFusionTarget dataFusionEntity) {
        String radarTargetId = dataFusionEntity.getRadarTargetId();
        // 自动处置时，调用引导
        String configValue = configService.getConfigValue("sys.countermeasure.auto_mode");
        if (StringUtils.isNoneBlank(configValue)
                && Boolean.parseBoolean(configValue)
                && StringUtils.isNoneBlank(radarTargetId)
                && radarTargetId.contains("_")) {

            String[] split = radarTargetId.split("_");
            if (split.length != 2) {
                return false;
            }

            String targetId = split[1];

            // 自动模式，出发引导
            GuidanceControlVo guidanceControlVo = new GuidanceControlVo();
            guidanceControlVo.setDataType(Constant.DEVICE_RADAR);
            guidanceControlVo.setTargetId("targetId");
            // 暂时没有用到，给个空值 防止算法报错
            guidanceControlVo.setDeviceId("");
            guidanceControlVo.setTargetNo(targetId);

            if (autoTaskRunning.get()) {
                cacheGuidanceControlVo = guidanceControlVo;
                return false;
            }

            this.autoTaskRunning(guidanceControlVo);
            return true;
        }
        if (StringUtils.isNoneBlank(configValue)
                && !Boolean.parseBoolean(configValue)) {
            cacheGuidanceControlVo = null;
        }

        return false;
    }


    public void dataRadarForward(List<DataRadarTarget> radarTargets) {
        // 缓存雷达最后一次发送数据的缓存
        radarTargets
                .stream()
                .filter(e -> e.getTargetId() != null && e.getTargetId() > 0)
                .forEach(radarTarget -> {
                    radarLastTime.put(radarTarget.getTargetId() + "", System.currentTimeMillis());
                });

        CompletableFuture.runAsync(() -> {
            GuidanceControlVo guidanceControlVo = dataMap.get(Constant.DEVICE_RADAR);
            if (guidanceControlVo == null) {
                return;
            }
            radarTargets.stream()
                    .filter(e -> Objects.equals(e.getTargetId() + "", guidanceControlVo.getTargetNo()))
                    .forEach(data -> {
                        String targetId = String.valueOf(data.getTargetId());
                        String deviceId = data.getDeviceId();
                        BigDecimal range = data.getRange();
                        BigDecimal targetLat = data.getTargetLat();
                        BigDecimal targetLon = data.getTargetLon();
                        BigDecimal altitude = data.getAltitude();

                        JSONObject jsonObject = getJsonObject(targetId,
                                deviceId,
                                range,
                                targetLat,
                                targetLon,
                                altitude,
                                Constant.DEVICE_RADAR,
                                this.currentAzimuth,
                                this.currentElevation,
                                data.getTargetBatch() + "");
                        this.broadcast(jsonObject);
                    });
        });
    }

    private static @NonNull JSONObject getJsonObject(String targetId,
                                                     String deviceId,
                                                     Object range,
                                                     Object targetLat,
                                                     Object targetLon,
                                                     Object altitude,
                                                     String dataType,
                                                     double azimuth,
                                                     double elevation,
                                                     String batchId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("tarid", targetId);
        jsonObject.putOpt("device_id", deviceId);
        jsonObject.putOpt("target_distance", range);

        jsonObject.putOpt("latitude", targetLat);
        jsonObject.putOpt("longitude", targetLon);
        jsonObject.putOpt("altitude", altitude);
        jsonObject.putOpt("data_type", dataType);
        jsonObject.putOpt("batchId", batchId);



        jsonObject.putOpt("azimuth", azimuth);
        jsonObject.putOpt("elevation", elevation);
        log.info(" -->  引导开始 目标： {} 数据来源：{}", targetId, dataType);
        return jsonObject;
    }


    private @NonNull String getCurrentTargetId() {
        AtomicReference<String> targetId = new AtomicReference<>("");

        dataMap.values().stream().findFirst().ifPresent(vo -> {
            targetId.set(vo.getTargetNo());
        });
        return targetId.get();
    }

    private @Nullable GuidanceControlVo getGuidanceControlVo() {
        return dataMap.values().stream().findFirst().orElse(null);
    }

    private @NonNull String getCurrentDataType() {
        AtomicReference<String> dataType = new AtomicReference<>("");
        dataMap.keySet().stream().findFirst().ifPresent(dataType::set);
        return dataType.get();
    }


    private void autoTaskRunning(GuidanceControlVo guidanceControlVo) {
        autoTaskRunning.set(true);
        this.guidanceControl(guidanceControlVo);
        scheduler.scheduleAtFixedRate(() -> {
            // 停止
            guidanceControlVo.setDataType("GuidanceStop");
            this.guidanceControl(guidanceControlVo);

            // 运行状态 false
            autoTaskRunning.set(false);

            // 当前缓存
            // 判断最后一次 雷达数据是否超过10秒，超过则清空
            if (cacheGuidanceControlVo != null && isExpired(cacheGuidanceControlVo)) {
                autoTaskRunning(cacheGuidanceControlVo);
            } else {
                cacheGuidanceControlVo = null;
            }
        }, 0, this.videoRecordingDuration, TimeUnit.SECONDS);
    }

    private boolean isExpired(GuidanceControlVo guidanceControlVo) {
        String targetNo = cacheGuidanceControlVo.getTargetNo();
        Long lastTime = this.radarLastTime.get(targetNo);
        if (lastTime == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();

        // 超过10秒
        if(currentTime - lastTime > 10000){
            return false;
        }

        return true;
    }

    private void init(){
        // 从数据库config表获取修正量值
        List<Config> configKeys = configService.getConfigKeys(List.of(
                "sys.OFD.rangeDDeviation",
                "sys.OFD.azimuthDeviation",
                "sys.OFD.elevationDeviation",
                "sys.OFD.videoRecordingDuration",
                "sys.radar.northAngle",
                "sys.photoelectric.device",
                "sys.OFD.videoRecordingDuration"));

        Config photoelectricDevice = ConfigUtils.getConfig.apply("sys.photoelectric.device", configKeys);


        if (photoelectricDevice == null || StringUtils.isEmpty(photoelectricDevice.getConfigValue())) {
            throw new IllegalArgumentException("请配置光电设备（经纬高）【sys.photoelectric.device】");
        }


        String[] split = photoelectricDevice.getConfigValue().split(",");

        if (split.length != 3) {
            throw new IllegalArgumentException("请配置光电设备（经纬高）【sys.photoelectric.device】");
        }
        double lat = Double.parseDouble(split[0]);
        double lon = Double.parseDouble(split[1]);
        double altitude = Double.parseDouble(split[2]);
        this.azimuthElevationCalculator = new AzimuthElevationCalculatorNew(lat, lon, altitude);

        Config videoRecordingDuration = ConfigUtils.getConfig.apply("sys.OFD.videoRecordingDuration", configKeys);
        this.videoRecordingDuration = videoRecordingDuration.getConfigValue() == null ? 5 : Integer.valueOf(videoRecordingDuration.getConfigValue());

        Config rangeDDeviation = ConfigUtils.getConfig.apply("sys.OFD.rangeDDeviation", configKeys);
        Config azimuthDeviation = ConfigUtils.getConfig.apply("sys.OFD.azimuthDeviation", configKeys);
        Config elevationDeviation = ConfigUtils.getConfig.apply("sys.OFD.elevationDeviation", configKeys);
        Config northAngle = ConfigUtils.getConfig.apply("sys.radar.northAngle", configKeys);

        this.targetDistanceCorrection = (int) Double.parseDouble(rangeDDeviation == null ? "0" : rangeDDeviation.getConfigValue());
        this.targetAzimuthCorrection = Double.parseDouble(azimuthDeviation == null ? "0" : azimuthDeviation.getConfigValue());
        this.targetElevationCorrection = Double.parseDouble(elevationDeviation == null ? "0" : elevationDeviation.getConfigValue());
        this.northAngleVal = Double.parseDouble(northAngle == null ? "0" : northAngle.getConfigValue());
    }

}
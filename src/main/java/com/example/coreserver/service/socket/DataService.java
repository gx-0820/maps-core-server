package com.example.coreserver.service.socket;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.coreserver.entity.DataOfd;
import com.example.coreserver.entity.DataRadarTarget;
import com.example.coreserver.entity.DataTdoaTarget;
import com.example.coreserver.entity.PhotoelectricFileRecord;
import com.example.coreserver.handler.RadarDataHandler;
import com.example.coreserver.service.DataOfdService;
import com.example.coreserver.service.DataRadarTargetService;
import com.example.coreserver.service.DataTdoaTargetService;
import com.example.coreserver.service.PhotoelectricFileRecordService;
import com.example.coreserver.service.algorithm.AlgorithmGrpcClient;
import com.example.coreserver.service.business.GeofenceService;
import com.example.coreserver.service.threat.ThreatAssessmentService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.example.coreserver.Constant.*;
import static com.example.coreserver.Constant.DEVICE_PHOTOELECTRIC;

@Slf4j
@Service
public class DataService {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    private final GeofenceService geofenceService;
    private final AlgorithmGrpcClient algorithmGrpcClient;
    private final RadarDataHandler radarDataHandler;

    private final DataRadarTargetService dataRadarTargetService;
    private final DataTdoaTargetService dataTdoaTargetService;
    private final DataOfdService dataOfdService;
    private final PhotoelectricFileRecordService photoelectricFileRecordService;

    private final ThreatAssessmentService threatAssessmentService;
    private final ServerDataClientHandler serverDataClientHandler;

    private final GuidanceDataClientHandler guidanceDataClientHandler;

    public DataService(GeofenceService geofenceService, AlgorithmGrpcClient algorithmGrpcClient, RadarDataHandler radarDataHandler, DataRadarTargetService dataRadarTargetService, DataTdoaTargetService dataTdoaTargetService, DataOfdService dataOfdService, PhotoelectricFileRecordService photoelectricFileRecordService, ThreatAssessmentService threatAssessmentService, ServerDataClientHandler serverDataClientHandler, GuidanceDataClientHandler guidanceDataClientHandler) {
        this.geofenceService = geofenceService;
        this.algorithmGrpcClient = algorithmGrpcClient;
        this.radarDataHandler = radarDataHandler;
        this.dataRadarTargetService = dataRadarTargetService;
        this.dataTdoaTargetService = dataTdoaTargetService;
        this.dataOfdService = dataOfdService;
        this.photoelectricFileRecordService = photoelectricFileRecordService;
        this.threatAssessmentService = threatAssessmentService;
        this.serverDataClientHandler = serverDataClientHandler;
        this.guidanceDataClientHandler = guidanceDataClientHandler;
    }


    public void handelMessage(String message, long startTime) {
        //  异步执行
        CompletableFuture.runAsync(() -> {
            try {
                JsonNode originalData = mapper.readTree(message);
                ObjectNode mergedData = mapper.createObjectNode();
                mergedData.put("timestamp", System.currentTimeMillis());
                mergedData.set("rawData", originalData);
                String type = originalData.get("type").asText();


                switch (type) {
                    case DEVICE_TDOA:
                        DataTdoaTarget dataTdoaTarget = JSON.parseObject(originalData.toString(), DataTdoaTarget.class);

                        // 推送TDOA数据
//                        algorithmGrpcClient.PushFusionData(mergedData.toString());
                        pushData(mergedData);
                        // 批量保存数据
                        dataTdoaTargetService.saveBatchData(dataTdoaTarget);

                        // 危险等级计算
                        threatAssessmentService.tdoaDataHandel(dataTdoaTarget);

                        this.guidanceDataClientHandler.dataTDOAForward(dataTdoaTarget);
                        break;
                    case DEVICE_RADAR:
                        // 数据上报
                        radarDataHandler.handleRadarData2Silas(JSONObject.parseObject(message));

                        List<DataRadarTarget> radarTargets = dataRadarTargetService.convertToDataRadarTargetList(message);

                        // 保存数据
                        dataRadarTargetService.saveBatchData(radarTargets);
                        // 危险等级计算
                        threatAssessmentService.radarDataHandel(radarTargets);

                        pushData(mergedData);

                        this.guidanceDataClientHandler.dataRadarForward(radarTargets);
                        break;
                    case DEVICE_PHOTOELECTRIC:
                        // 光电数据
                        DataOfd dataOfd = dataOfdService.convertToDataOfd(originalData);
                        dataOfdService.saveBatchData(dataOfd);
                        // 转发到前端
                        this.serverDataClientHandler.broadcast(DEVICE_PHOTOELECTRIC, dataOfd);
                        break;
                    case "Guidance":
                        PhotoelectricFileRecord photoelectricFileRecord = JSON.parseObject(originalData.toString(), PhotoelectricFileRecord.class);

                        // 引导流程完成后的处理
//                        this.guidanceDataClientHandler.removeGuidance(photoelectricFileRecord);

                        photoelectricFileRecordService.save(photoelectricFileRecord);
                        break;
                    default:
//                        defaultHandel(mergedData, type, message);
//                        System.out.printf("message" + message);
                }
            } catch (Exception e) {
                log.error("Message processing failed in {}ms | Error: {}",
                        System.currentTimeMillis() - startTime,
                        e.getMessage(), e);
            }
        }, ThreadUtil.newExecutor(20));
    }


    private void pushData(ObjectNode mergedData) {
        // 使用缓存的地理围栏数据
//        geofenceCacheLock.readLock().lock();
//        try {
//            if (cachedGeofences != null) {
//                mergedData.set("geofence", cachedGeofences);
//            } else {
//                log.warn("Geofence cache is empty, skipping geofence data.");
//            }
//        } finally {
//            geofenceCacheLock.readLock().unlock();
//        }

        // 添加一个空对象，防止算法报错
        ArrayNode jsonNodes = mapper.createArrayNode();
        mergedData.set("geofence", jsonNodes);
//        log.info("Merged data: {}", mergedData);

        // 推送数据到 gRPC 服务
        // 推送给融合的数据接口
        algorithmGrpcClient.PushFusionData(mergedData.toString());
        // 推送给轨迹预测的数据接口
        algorithmGrpcClient.PushTrackData(mergedData.toString());
    }
}

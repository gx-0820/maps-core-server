package com.example.coreserver.service.threat;

import com.alibaba.fastjson2.JSONObject;
import com.example.coreserver.entity.Config;
import com.example.coreserver.entity.DataFusionTarget;
import com.example.coreserver.entity.DataRadarTarget;
import com.example.coreserver.entity.DataTdoaTarget;
import com.example.coreserver.entity.threat.ThreatAssessmentArgs;
import com.example.coreserver.entity.threat.ThreatAssessmentResult;
import com.example.coreserver.service.device.ConfigService;
import com.example.coreserver.service.socket.ServerDataClientHandler;
import com.example.coreserver.utils.ConfigUtils;
import com.example.coreserver.utils.ThreatAssessmentUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static com.example.coreserver.Constant.*;

@Service
public class ThreatAssessmentService {

    private final ThreatAssessmentUtil threatAssessmentUtil;
    private final ServerDataClientHandler serverDataClientHandler;
    private JSONObject alarmWeight;
    private String alarmEnable;

    public ThreatAssessmentService(ThreatAssessmentUtil threatAssessmentUtil, ServerDataClientHandler serverDataClientHandler, ConfigService configService) {
        this.threatAssessmentUtil = threatAssessmentUtil;
        this.serverDataClientHandler = serverDataClientHandler;

        List<String> keys = List.of("sys.alarm.weight", "sys.alarm.enable");
        List<Config> configKeys = configService.getConfigKeys(keys);

        Config weight = ConfigUtils.getConfig.apply("sys.alarm.weight", configKeys);
        Config enable = ConfigUtils.getConfig.apply("sys.alarm.enable", configKeys);

        this.alarmWeight = weight != null ? JSONObject.parseObject(weight.getConfigValue(), JSONObject.class) : null;
        this.alarmEnable = enable != null ? enable.getConfigValue() : null;
    }

    /**
     * 雷达数据 危险等级计算
     *
     */
    public void radarDataHandel(List<DataRadarTarget> radarTargets) {

        radarTargets.forEach(target -> {
            // 计算威胁等级
            JSONObject result = new JSONObject();
            result.put("target", target);
            result.put("dataType", THREAT_RADAR);

            Date timestamp = target.getTimestamp();
            BigDecimal speed = target.getSpeed();
            BigDecimal targetLon = target.getTargetLon();
            BigDecimal targetLat = target.getTargetLat();
            BigDecimal altitude = target.getAltitude();
            String targetId = buildRadarTargetId(target);

            ThreatAssessmentResult evaluate = evaluate(
                    targetId,
                    ThreatAssessmentArgs.TargetType.RADAR,
                    false,
                    timestamp,
                    speed,
                    targetLon,
                    targetLat,
                    altitude,
                    result
            );
            serverDataClientHandler.broadcast(THREAT_RADAR, result);
        });

    }

    /**
     * TDOA数据计算威胁等级
     */
    public void tdoaDataHandel(DataTdoaTarget target) {
        // 计算威胁等级 推送雷达数据到前端
        JSONObject result = new JSONObject();
        result.put("target", target);
        result.put("dataType", THREAT_TDOA);

        Date timestamp = target.getTimestamp();
        BigDecimal speed = target.getVelocity();
        BigDecimal targetLon = target.getUavLon();
        BigDecimal targetLat = target.getUavLat();
        BigDecimal altitude = target.getUavHeight();
        String targetId = buildTdoaTargetId(target);
        boolean whiteList = isWhiteList(target);

        ThreatAssessmentResult evaluate = evaluate(
                targetId,
                ThreatAssessmentArgs.TargetType.TDOA,
                whiteList,
                timestamp,
                speed,
                targetLon,
                targetLat,
                altitude,
                result
        );
        serverDataClientHandler.broadcast(THREAT_TDOA, result);
    }

    /**
     * 融合数据计算威胁等级
     *
     * @param dataFusionEntity
     */
    public void algorithmDataHandel(DataFusionTarget dataFusionEntity) {
        JSONObject result = new JSONObject();
        result.put("target", dataFusionEntity);
        result.put("dataType", THREAT_FUSIONS);

        Date timestamp = dataFusionEntity.getTimestamp();
        BigDecimal speed = dataFusionEntity.getSpeed();
        BigDecimal targetLon = dataFusionEntity.getTargetLon();
        BigDecimal targetLat = dataFusionEntity.getTargetLat();
        BigDecimal altitude = dataFusionEntity.getAltitude();
        String targetId = dataFusionEntity.getTargetId();

        ThreatAssessmentResult evaluate = evaluate(
                targetId,
                ThreatAssessmentArgs.TargetType.RADAR,
                false,
                timestamp,
                speed,
                targetLon,
                targetLat,
                altitude,
                result
        );
        serverDataClientHandler.broadcast(THREAT_FUSIONS, result);
    }


    /**
     * 计算威胁等级
     *
     * @param timestamp 时间戳
     * @param speed     无人机速度（米/秒）
     * @param targetLon 经度坐标
     * @param targetLat 纬度坐标
     * @param altitude  高度（米）
     * @param result
     * @return
     */
    private ThreatAssessmentResult evaluate(String targetId,
                                            ThreatAssessmentArgs.TargetType targetType,
                                            boolean whiteList,
                                            Date timestamp,
                                            BigDecimal speed,
                                            BigDecimal targetLon,
                                            BigDecimal targetLat,
                                            BigDecimal altitude,
                                            JSONObject result) {

        LocalDateTime localDateTime = timestamp.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        ThreatAssessmentArgs args = ThreatAssessmentArgs.builder()
                .id(targetId)
                .speed(speed.doubleValue())
                .timestamp(localDateTime)
                .longitude(targetLon.doubleValue())
                .latitude(targetLat.doubleValue())
                .altitude(altitude.doubleValue())
                .targetType(targetType)
                .whiteList(whiteList)
                .build();

        ThreatAssessmentResult evaluate = threatAssessmentUtil.evaluate(args);


        ThreatAssessmentResult.ThreatLevel threatLevel = evaluate.getThreatLevel();
        Integer threatScore = evaluate.getThreatScore();

        int levelValue = -1;
        if (threatLevel != null) {
            levelValue = threatLevel.getValue();
        }

        // 威胁等级
        result.put("threatLevel", levelValue);
        result.put("threatScore", threatScore);
        result.put("threatArea", evaluate.getThreatAssessmentArea().getAreaName());
//        result.put("longitude", targetLon);
//        result.put("latitude", targetLat);
        result.put("alarmEnable", alarmEnable);
        result.put("alarmWeight", alarmWeight);

        return evaluate;
    }

    private String buildRadarTargetId(DataRadarTarget target) {
        if (target == null || target.getTargetBatch() == null || target.getTargetId() == null) {
            return null;
        }
        return target.getTargetBatch() + "_" + target.getTargetId();
    }


    private String buildTdoaTargetId(DataTdoaTarget target) {
        if (target == null || target.getUavId() == null || target.getUavId().isBlank()) {
            return null;
        }
        return target.getUavId() + "_" + target.getTraceId();
    }

    private boolean isWhiteList(DataTdoaTarget target) {
        if (target == null || target.getWhiteListId() == null) {
            return false;
        }
        return target.getWhiteListId() > 0;
    }


}

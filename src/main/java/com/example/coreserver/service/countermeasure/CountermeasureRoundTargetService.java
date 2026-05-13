package com.example.coreserver.service.countermeasure;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coreserver.entity.DataRadarTarget;
import com.example.coreserver.entity.DataTdoaTarget;
import com.example.coreserver.entity.countermeasure.CountermeasureExecutionScenario;
import com.example.coreserver.entity.countermeasure.CountermeasureTargetDataSource;
import com.example.coreserver.entity.threat.ThreatAssessmentArgs;
import com.example.coreserver.entity.threat.ThreatAssessmentResult;
import com.example.coreserver.mapper.DataRadarTargetMapper;
import com.example.coreserver.mapper.DataTdoaTargetMapper;
import com.example.coreserver.repository.algorithm.DataFusionTargetRepository;
import com.example.coreserver.utils.ThreatAssessmentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 自动处置轮次目标读取与候选评估。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CountermeasureRoundTargetService {

    private static final int DEBUG_BYPASS_SCORE = 60;

    private final CountermeasureConfigService countermeasureConfigService;
    private final DataFusionTargetRepository dataFusionTargetRepository;
    private final DataRadarTargetMapper dataRadarTargetMapper;
    private final DataTdoaTargetMapper dataTdoaTargetMapper;
    private final ThreatAssessmentUtil threatAssessmentUtil;

    public List<AssessedTarget> evaluateRoundTargets(long roundDurationMs) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minus(Duration.ofMillis(roundDurationMs));
        CountermeasureTargetDataSource dataSource = countermeasureConfigService.getTargetDataSource();
        CountermeasureExecutionScenario scenario = countermeasureConfigService.getExecutionScenario();
        List<TargetSnapshot> roundTargets = loadRoundTargets(dataSource, start, end);
        log.info("自动处置轮次读取完成: source={}, scenario={}, windowStart={}, windowEnd={}, rawCount={}",
                dataSource, scenario, start, end, roundTargets.size());

        Map<String, TargetSnapshot> latestById = new LinkedHashMap<>();
        for (TargetSnapshot target : roundTargets) {
            log.info("自动处置读取原始目标: {}", target.rawSummary());
            if (target.id() == null || target.id().isBlank()) {
                log.warn("自动处置目标已过滤: source={}, rowId={}, reason=目标唯一标识为空", target.dataSource(), target.sourceRecordId());
                continue;
            }
            if (!target.hasCoordinates()) {
                log.warn("自动处置目标已过滤: source={}, targetId={}, rowId={}, reason=经纬度缺失, raw={}",
                        target.dataSource(), target.id(), target.sourceRecordId(), target.rawSummary());
                continue;
            }
            TargetSnapshot existing = latestById.get(target.id());
            if (existing != null) {
                log.info("自动处置目标忽略旧记录: source={}, targetId={}, keptTimestamp={}, skippedTimestamp={}",
                        target.dataSource(), target.id(), existing.timestamp(), target.timestamp());
                continue;
            }
            latestById.put(target.id(), target);
        }

        List<AssessedTarget> candidates = new ArrayList<>();
        for (TargetSnapshot target : latestById.values()) {
            AssessedTarget assessedTarget = assessTarget(target, scenario);
            if (assessedTarget != null) {
                candidates.add(assessedTarget);
            }
        }
        return candidates;
    }

    private List<TargetSnapshot> loadRoundTargets(
            CountermeasureTargetDataSource dataSource,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return switch (dataSource) {
            case RADAR -> loadRadarTargets(start, end);
            case TDOA -> loadTdoaTargets(start, end);
            case FUSION -> loadFusionTargets(start, end);
        };
    }

    private List<TargetSnapshot> loadFusionTargets(LocalDateTime start, LocalDateTime end) {
        return dataFusionTargetRepository.findByTimestampBetweenOrderByTimestampDesc(start, end).stream()
                .map(entity -> {
                    String targetId = buildCompositeTargetId(entity.getTargetBatch(), entity.getTargetId());
                    return new TargetSnapshot(
                            targetId,
                            entity.getId(),
                            CountermeasureTargetDataSource.FUSION,
                            ThreatAssessmentArgs.TargetType.FUSION,
                            entity.getTimestamp(),
                            entity.getTargetLon(),
                            entity.getTargetLat(),
                            entity.getAltitude(),
                            entity.getSpeed(),
                            entity.getRange(),
                            entity.getAzimuth(),
                            shouldTreatAsWhiteList(entity.getWhiteListId()),
                            false,
                            String.format(
                                    "source=%s,targetId=%s,rowId=%s,timestamp=%s,targetBatch=%s,targetInnerId=%s,lon=%s,lat=%s,alt=%s,speed=%s,range=%s,azimuth=%s,whiteListId=%s",
                                    CountermeasureTargetDataSource.FUSION,
                                    targetId,
                                    entity.getId(),
                                    entity.getTimestamp(),
                                    entity.getTargetBatch(),
                                    entity.getTargetId(),
                                    entity.getTargetLon(),
                                    entity.getTargetLat(),
                                    entity.getAltitude(),
                                    entity.getSpeed(),
                                    entity.getRange(),
                                    entity.getAzimuth(),
                                    entity.getWhiteListId()
                            )
                    );
                })
                .toList();
    }

    private List<TargetSnapshot> loadRadarTargets(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<DataRadarTarget> queryWrapper = new LambdaQueryWrapper<DataRadarTarget>()
                .between(DataRadarTarget::getTimestamp, toDate(start), toDate(end))
                .and(wrapper -> wrapper.eq(DataRadarTarget::getIsDelete, 0).or().isNull(DataRadarTarget::getIsDelete))
                .orderByDesc(DataRadarTarget::getTimestamp);
        return dataRadarTargetMapper.selectList(queryWrapper).stream()
                .map(entity -> {
                    String targetId = buildCompositeTargetId(entity.getTargetBatch(), entity.getTargetId());
                    return new TargetSnapshot(
                            targetId,
                            entity.getId(),
                            CountermeasureTargetDataSource.RADAR,
                            ThreatAssessmentArgs.TargetType.RADAR,
                            toLocalDateTime(entity.getTimestamp()),
                            entity.getTargetLon(),
                            entity.getTargetLat(),
                            entity.getAltitude(),
                            entity.getSpeed(),
                            entity.getRange(),
                            entity.getAzimuth2(),
                            false,
                            false,
                            String.format(
                                    "source=%s,targetId=%s,rowId=%s,timestamp=%s,targetBatch=%s,targetInnerId=%s,lon=%s,lat=%s,alt=%s,speed=%s,range=%s,azimuth=%s,isDelete=%s",
                                    CountermeasureTargetDataSource.RADAR,
                                    targetId,
                                    entity.getId(),
                                    entity.getTimestamp(),
                                    entity.getTargetBatch(),
                                    entity.getTargetId(),
                                    entity.getTargetLon(),
                                    entity.getTargetLat(),
                                    entity.getAltitude(),
                                    entity.getSpeed(),
                                    entity.getRange(),
                                    entity.getAzimuth2(),
                                    entity.getIsDelete()
                            )
                    );
                })
                .toList();
    }

    private List<TargetSnapshot> loadTdoaTargets(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<DataTdoaTarget> queryWrapper = new LambdaQueryWrapper<DataTdoaTarget>()
                .between(DataTdoaTarget::getTimestamp, toDate(start), toDate(end))
                .orderByDesc(DataTdoaTarget::getTimestamp);
        return dataTdoaTargetMapper.selectList(queryWrapper).stream()
                .map(entity -> {
                    String targetId = buildCompositeTargetId(entity.getUavId(), entity.getTraceId());
                    return new TargetSnapshot(
                            targetId,
                            entity.getId(),
                            CountermeasureTargetDataSource.TDOA,
                            ThreatAssessmentArgs.TargetType.TDOA,
                            toLocalDateTime(entity.getTimestamp()),
                            entity.getUavLon(),
                            entity.getUavLat(),
                            entity.getUavAlt(),
                            entity.getVelocity(),
                            entity.getUavDistance(),
                            entity.getUavAzimuth(),
                            shouldTreatAsWhiteList(entity.getWhiteListId()),
                            false,
                            String.format(
                                    "source=%s,targetId=%s,rowId=%s,timestamp=%s,uavId=%s,traceId=%s,lon=%s,lat=%s,alt=%s,speed=%s,distance=%s,azimuth=%s,whiteListId=%s,targetType=%s",
                                    CountermeasureTargetDataSource.TDOA,
                                    targetId,
                                    entity.getId(),
                                    entity.getTimestamp(),
                                    entity.getUavId(),
                                    entity.getTraceId(),
                                    entity.getUavLon(),
                                    entity.getUavLat(),
                                    entity.getUavAlt(),
                                    entity.getVelocity(),
                                    entity.getUavDistance(),
                                    entity.getUavAzimuth(),
                                    entity.getWhiteListId(),
                                    entity.getTargetType()
                            )
                    );
                })
                .toList();
    }

    private AssessedTarget assessTarget(TargetSnapshot target, CountermeasureExecutionScenario scenario) {
        ThreatAssessmentArgs args = buildAssessmentArgs(target);
        try {
            ThreatAssessmentResult rawResult = threatAssessmentUtil.evaluate(args);
            ThreatAssessmentResult effectiveResult = normalizeAssessmentForScenario(target, rawResult, scenario, null);
            if (!isCandidate(effectiveResult)) {
                log.info("自动处置目标已过滤: source={}, targetId={}, scenario={}, 原始结果={}, 生效结果={}, reason=威胁等级未进入候选集",
                        target.dataSource(), target.id(), scenario, describeResult(rawResult), describeResult(effectiveResult));
                return null;
            }
            log.info("自动处置目标进入候选集: source={}, targetId={}, scenario={}, 原始结果={}, 生效结果={}",
                    target.dataSource(), target.id(), scenario, describeResult(rawResult), describeResult(effectiveResult));
            return new AssessedTarget(
                    target.id(),
                    target,
                    target.timestamp(),
                    effectiveResult.getThreatLevel(),
                    effectiveResult.getThreatAssessmentArea(),
                    effectiveResult.getThreatScore() == null ? 0 : effectiveResult.getThreatScore(),
                    effectiveResult.isWhiteList()
            );
        } catch (Exception e) {
            ThreatAssessmentResult effectiveResult = normalizeAssessmentForScenario(target, null, scenario, e);
            if (!isCandidate(effectiveResult)) {
                log.warn("自动处置目标评估失败并被过滤: source={}, targetId={}, scenario={}, error={}",
                        target.dataSource(), target.id(), scenario, e.getMessage(), e);
                return null;
            }
            log.warn("威胁评估发生异常，但DEBUG场景放行目标进入候选集: source={}, targetId={}, error={}",
                    target.dataSource(), target.id(), e.getMessage(), e);
            return new AssessedTarget(
                    target.id(),
                    target,
                    target.timestamp(),
                    effectiveResult.getThreatLevel(),
                    effectiveResult.getThreatAssessmentArea(),
                    effectiveResult.getThreatScore() == null ? 0 : effectiveResult.getThreatScore(),
                    effectiveResult.isWhiteList()
            );
        }
    }

    private ThreatAssessmentArgs buildAssessmentArgs(TargetSnapshot target) {
        return ThreatAssessmentArgs.builder()
                .id(target.id())
                .targetType(target.targetType())
                .timestamp(target.timestamp())
                .whiteList(target.whiteList())
                .imageTransmission(target.imageTransmission())
                .speed(toDouble(target.speed()))
                .longitude(toDouble(target.longitude()))
                .latitude(toDouble(target.latitude()))
                .altitude(toDouble(target.altitude()))
                .build();
    }

    private ThreatAssessmentResult normalizeAssessmentForScenario(
            TargetSnapshot target,
            ThreatAssessmentResult rawResult,
            CountermeasureExecutionScenario scenario,
            Exception error
    ) {
        if (scenario != CountermeasureExecutionScenario.DEBUG) {
            return rawResult;
        }
        if (isCandidate(rawResult)) {
            return rawResult;
        }

        String reason;
        if (error != null) {
            reason = "威胁评估抛出异常";
        } else if (rawResult == null) {
            reason = "威胁评估结果为空";
        } else if (rawResult.isWhiteList()) {
            reason = "原始结果命中白名单";
        } else if (rawResult.getThreatLevel() == null) {
            reason = "原始结果缺少威胁等级";
        } else {
            reason = "原始结果威胁等级不在候选集";
        }
        ThreatAssessmentResult debugResult = buildDebugBypassResult(rawResult);
        log.info("DEBUG场景放行目标: source={}, targetId={}, reason={}, 原始结果={}, 放行结果={}",
                target.dataSource(), target.id(), reason, describeResult(rawResult), describeResult(debugResult));
        return debugResult;
    }

    private ThreatAssessmentResult buildDebugBypassResult(ThreatAssessmentResult rawResult) {
        ThreatAssessmentResult.ThreatAssessmentArea area = rawResult == null
                ? ThreatAssessmentResult.ThreatAssessmentArea.OUTSIDE
                : rawResult.getThreatAssessmentArea();
        if (area == null) {
            area = ThreatAssessmentResult.ThreatAssessmentArea.OUTSIDE;
        }
        ThreatAssessmentResult.ThreatLevel level = rawResult != null
                && rawResult.getThreatLevel() != null
                && rawResult.getThreatLevel() != ThreatAssessmentResult.ThreatLevel.NONE
                ? rawResult.getThreatLevel()
                : ThreatAssessmentResult.ThreatLevel.MEDIUM;
        int score = rawResult != null
                && rawResult.getThreatScore() != null
                && rawResult.getThreatScore() > 0
                ? rawResult.getThreatScore()
                : DEBUG_BYPASS_SCORE;
        return ThreatAssessmentResult.builder()
                .whiteList(false)
                .threatAssessmentArea(area)
                .threatLevel(level)
                .threatScore(score)
                .build();
    }

    private boolean isCandidate(ThreatAssessmentResult result) {
        if (result == null || result.isWhiteList() || result.getThreatLevel() == null) {
            return false;
        }
        return result.getThreatLevel() == ThreatAssessmentResult.ThreatLevel.HIGH
                || result.getThreatLevel() == ThreatAssessmentResult.ThreatLevel.MEDIUM
                || result.getThreatLevel() == ThreatAssessmentResult.ThreatLevel.LOW;
    }

    private boolean shouldTreatAsWhiteList(Integer whiteListId) {
        return whiteListId != null && whiteListId > 0;
    }

    private double toDouble(BigDecimal value) {
        return value == null ? 0.0D : value.doubleValue();
    }

    private String buildCompositeTargetId(Object batch, Object targetInnerId) {
        String batchPart = normalizeTargetKey(batch);
        String targetPart = normalizeTargetKey(targetInnerId);
        if (batchPart == null || targetPart == null) {
            return null;
        }
        return batchPart + "_" + targetPart;
    }

    private String normalizeTargetKey(Object rawKey) {
        if (rawKey == null) {
            return null;
        }
        String key = String.valueOf(rawKey).trim();
        if (key.isEmpty()) {
            return null;
        }
        return key;
    }

    private LocalDateTime toLocalDateTime(Date value) {
        if (value == null) {
            return LocalDateTime.now();
        }
        return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private Date toDate(LocalDateTime value) {
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    private String describeResult(ThreatAssessmentResult result) {
        if (result == null) {
            return "null";
        }
        return String.format(
                "area=%s,level=%s,score=%s,whiteList=%s",
                result.getThreatAssessmentArea(),
                result.getThreatLevel(),
                result.getThreatScore(),
                result.isWhiteList()
        );
    }

    public record AssessedTarget(
            String id,
            TargetSnapshot snapshot,
            LocalDateTime timestamp,
            ThreatAssessmentResult.ThreatLevel threatLevel,
            ThreatAssessmentResult.ThreatAssessmentArea threatAssessmentArea,
            int threatScore,
            boolean whiteList
    ) {
    }

    public record TargetSnapshot(
            String id,
            String sourceRecordId,
            CountermeasureTargetDataSource dataSource,
            ThreatAssessmentArgs.TargetType targetType,
            LocalDateTime timestamp,
            BigDecimal longitude,
            BigDecimal latitude,
            BigDecimal altitude,
            BigDecimal speed,
            BigDecimal range,
            BigDecimal azimuth,
            boolean whiteList,
            boolean imageTransmission,
            String rawSummary
    ) {
        public boolean hasCoordinates() {
            return longitude != null && latitude != null;
        }
    }
}

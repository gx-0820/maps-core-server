package com.example.coreserver.utils;

import cn.hutool.cache.impl.CacheObj;
import cn.hutool.cache.impl.LRUCache;
import com.example.coreserver.entity.Config;
import com.example.coreserver.entity.threat.ThreatAssessmentArgs;
import com.example.coreserver.entity.threat.ThreatAssessmentResult;
import com.example.coreserver.entity.threat.ThreatAssessmentResult.ThreatAssessmentArea;
import com.example.coreserver.repository.ConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author : [wangminan]
 * @description : 威胁评估工具类
 */
@Slf4j
@Component
public class ThreatAssessmentUtil {

    // LRU中同时缓存的最多UAV数量
    private static final int UAV_CACHE_CAPACITY = 100;
    // 每一架UAV在LRU中最多缓存的帧数
    private static final int MAX_TRAJECTORY_FRAMES = 6;
    // 从数据库重载区域配置的间隔，过短可能增加数据库负担，过长可能导致评估结果滞后，这里设置为 10 秒。
    private static final long REFRESH_INTERVAL_MILLIS = 10_000L;
    // 参与机群判定的活跃目标时间窗（秒），超出该时间的历史目标不再参与本次机群计算
    private static final long ACTIVE_TARGET_WINDOW_SECONDS = 10L;
    // 方向分析所使用的历史轨迹时间窗（秒），用于判断目标是否朝向反制区
    private static final long DIRECTION_HISTORY_WINDOW_SECONDS = 10L;
    // 进入中威胁速度判定的阈值（米/秒）
    private static final double MEDIUM_SPEED_THRESHOLD_METERS_PER_SECOND = 10.0D;
    // 机群判定中任意两目标允许的最大三维距离阈值（米）
    private static final double SWARM_DISTANCE_THRESHOLD_METERS = 50.0D;
    // 机群判定的最小规模（含当前目标）
    private static final int SWARM_MIN_SIZE = 3;
    // 机群判定的最大规模（含当前目标），避免过大集合导致误判或计算开销过高
    private static final int SWARM_MAX_SIZE = 16;
    // 判定“朝向反制区”时的最大允许夹角阈值（度）
    private static final double DIRECTION_ANGLE_THRESHOLD_DEGREES = 45.0D;

    private volatile boolean prepared;

    private final ReadWriteLock areaConfigLock = new ReentrantReadWriteLock();

    /**
     * 当前区域配置快照。
     */
    private volatile ThreatAssessmentAreaSupport.AreaConfigSnapshot areaConfigSnapshot = ThreatAssessmentAreaSupport.AreaConfigSnapshot.empty();

    /**
     * 一个本地的UAV缓存，用于评估是否是机群。
     * Hutool 的 LRUCache 内部已经做了锁控制，这里额外在工具类上同步，
     * 是为了保证“取轨迹-追加新轨迹-写回缓存”这个复合操作的原子性。
     */
    private final LRUCache<String, List<ThreatAssessmentArgs>> uavCache = new LRUCache<>(UAV_CACHE_CAPACITY);

    private final ConfigRepository configRepository;
    private final ThreatAssessmentAreaSupport threatAssessmentAreaSupport;

    @Autowired
    public ThreatAssessmentUtil(ConfigRepository configRepository, ObjectMapper objectMapper) {
        this.configRepository = configRepository;
        this.threatAssessmentAreaSupport = new ThreatAssessmentAreaSupport(objectMapper);
    }

    /**
     * 加锁初始化，首次使用前完成缓存和区域配置加载。
     */
    private void initUtil() {
        if (!prepared) {
            synchronized (this) {
                if (!prepared) {
                    refreshAreaConfig();
                    prepared = true;
                }
            }
        }
    }

    /**
     * 每 10 秒从数据库重载一次电子围栏配置。
     * 读数据库和解析 JSON 放在锁外执行，只有最终替换快照时进入写锁，
     * 这样并发 evaluate 基本只会在极短时间内被写锁阻塞。
     */
    @Scheduled(fixedDelay = REFRESH_INTERVAL_MILLIS, initialDelay = REFRESH_INTERVAL_MILLIS)
    public void refreshAreaConfigPeriodically() {
        try {
            initUtil();
            refreshAreaConfig();
        } catch (Exception e) {
            log.error("威胁评估区域配置定时刷新失败: {}", e.getMessage(), e);
        }
    }

    private void refreshAreaConfig() {
        List<Config> configList = loadThreatAssessmentConfigs();
        ThreatAssessmentAreaSupport.AreaConfigSnapshot newSnapshot = threatAssessmentAreaSupport.buildAreaConfigSnapshot(configList);
        areaConfigLock.writeLock().lock();
        try {
            log.info("开始替换威胁评估区域快照: configCount={}, refreshedAt={}", configList.size(), newSnapshot.refreshedAtMillis());
            this.areaConfigSnapshot = newSnapshot;
        } finally {
            areaConfigLock.writeLock().unlock();
            log.info("威胁评估区域快照替换完成");
        }
    }

    protected List<Config> loadThreatAssessmentConfigs() {
        if (configRepository == null) {
            log.warn("威胁评估配置仓库为空，使用空区域配置");
            return Collections.emptyList();
        }
        List<Config> configList = configRepository.findByConfigKeys(List.of(
                ThreatAssessmentAreaSupport.COUNTERMEASURE_CONFIG_KEY,
                ThreatAssessmentAreaSupport.WARNING_CONFIG_KEY,
                ThreatAssessmentAreaSupport.DETECTION_CONFIG_KEY
        ));
        for (Config config : configList) {
            log.info("从config表读取威胁评估配置: key={}, name={}, raw={}",
                    config.getConfigKey(), config.getConfigName(), threatAssessmentAreaSupport.summarizeConfigValue(config.getConfigValue()));
        }
        return configList;
    }

    private ThreatAssessmentAreaSupport.AreaConfigSnapshot snapshotAreaConfig() {
        areaConfigLock.readLock().lock();
        try {
            return this.areaConfigSnapshot;
        } finally {
            areaConfigLock.readLock().unlock();
        }
    }

    /**
     * 核心方法 需要接收并发调用
     *
     * @param args 入参
     * @return 评估结果 包括威胁等级与打分
     */
    public ThreatAssessmentResult evaluate(ThreatAssessmentArgs args) {
        Objects.requireNonNull(args, "ThreatAssessmentArgs must not be null");
        initUtil();
        log.info("开始威胁评估: {}", ThreatAssessmentLogFormatter.describeArgs(args));

        ThreatAssessmentAreaSupport.AreaConfigSnapshot areaSnapshot = snapshotAreaConfig();
        List<ThreatAssessmentArgs> history = getTrajectoryHistory(args.getId());
        ThreatAssessmentArea currentArea = threatAssessmentAreaSupport.resolveArea(args, areaSnapshot);
        boolean unidentifiableTarget = isUnidentifiableTarget(args);
        boolean movingTowardCountermeasure = isMovingTowardCountermeasure(args, history, areaSnapshot);
        boolean swarmTarget = isSwarmTarget(args);

        ThreatAssessmentResult result;
        if (args.isWhiteList()) {
            log.info("威胁评估命中白名单: targetId={}, currentArea={}", args.getId(), currentArea);
            result = buildEmptyResult(true, currentArea);
        } else if (unidentifiableTarget) {
            log.info("威胁评估命中不可识别目标规则: targetId={}, currentArea={}, targetType={}, imageTransmission={}",
                    args.getId(), currentArea, args.getTargetType(), args.isImageTransmission());
            result = ThreatAssessmentResult.builder()
                    .whiteList(false)
                    .threatAssessmentArea(currentArea)
                    .threatLevel(ThreatAssessmentResult.ThreatLevel.HIGH)
                    .threatScore(100)
                    .build();
        } else {
            ThreatAssessmentResult.ThreatLevel threatLevel = determineThreatLevel(
                    args,
                    currentArea,
                    movingTowardCountermeasure,
                    swarmTarget
            );
            Integer threatScore = calculateThreatScore(
                    threatLevel,
                    args,
                    movingTowardCountermeasure,
                    swarmTarget,
                    areaSnapshot
            );
            result = ThreatAssessmentResult.builder()
                    .whiteList(false)
                    .threatAssessmentArea(currentArea)
                    .threatLevel(threatLevel)
                    .threatScore(threatScore)
                    .build();
        }

        updateLRUCache(args);
        log.info("威胁评估完成: targetId={}, currentArea={}, historySize={}, movingTowardCountermeasure={}, swarmTarget={}, result={}",
                args.getId(), currentArea, history.size(), movingTowardCountermeasure, swarmTarget, ThreatAssessmentLogFormatter.describeResult(result));
        return result;
    }

    /**
     * 更新 LRUCache。
     * 对同一个目标保留最近 N 帧轨迹，便于评估“朝向反制区”和“机群”。
     */
    private void updateLRUCache(ThreatAssessmentArgs args) {
        if (args.getId() == null) {
            return;
        }
        synchronized (this.uavCache) {
            List<ThreatAssessmentArgs> trajectory = this.uavCache.get(args.getId());
            List<ThreatAssessmentArgs> updatedTrajectory = trajectory == null
                    ? new ArrayList<>()
                    : new ArrayList<>(trajectory);
            updatedTrajectory.add(copyArgs(args));
            if (updatedTrajectory.size() > MAX_TRAJECTORY_FRAMES) {
                updatedTrajectory = new ArrayList<>(
                        updatedTrajectory.subList(updatedTrajectory.size() - MAX_TRAJECTORY_FRAMES, updatedTrajectory.size())
                );
            }
            this.uavCache.put(args.getId(), updatedTrajectory);
        }
    }

    private ThreatAssessmentResult.ThreatLevel determineThreatLevel(
            ThreatAssessmentArgs args,
            ThreatAssessmentArea currentArea,
            boolean movingTowardCountermeasure,
            boolean swarmTarget
    ) {
        if (currentArea == ThreatAssessmentArea.COUNTERMEASURE) {
            return ThreatAssessmentResult.ThreatLevel.HIGH;
        }
        if (currentArea == ThreatAssessmentArea.WARNING) {
            if (swarmTarget) {
                return ThreatAssessmentResult.ThreatLevel.MEDIUM;
            }
            if (args.getSpeed() >= MEDIUM_SPEED_THRESHOLD_METERS_PER_SECOND && movingTowardCountermeasure) {
                return ThreatAssessmentResult.ThreatLevel.MEDIUM;
            }
            return ThreatAssessmentResult.ThreatLevel.LOW;
        }
        return ThreatAssessmentResult.ThreatLevel.NONE;
    }

    private Integer calculateThreatScore(
            ThreatAssessmentResult.ThreatLevel threatLevel,
            ThreatAssessmentArgs args,
            boolean movingTowardCountermeasure,
            boolean swarmTarget,
            ThreatAssessmentAreaSupport.AreaConfigSnapshot areaSnapshot
    ) {
        if (threatLevel == null || threatLevel == ThreatAssessmentResult.ThreatLevel.NONE) {
            return ThreatAssessmentResult.ThreatLevel.NONE.getValue();
        }

        double distanceFactor = threatAssessmentAreaSupport.calculateDistanceFactor(args, areaSnapshot);
        double speedFactor = threatAssessmentAreaSupport.normalizeSpeed(args.getSpeed());
        double behaviorFactor = swarmTarget ? 1.0D : (movingTowardCountermeasure ? 0.8D : 0.0D);

        /*
         * 分段加权算法：
         * 1. 先按威胁等级切分分数区间，保证 HIGH > MEDIUM > LOW > NONE。
         * 2. 同等级内再做归一化加权排序：
         *    - distanceFactor：越靠近反制区几何中心，值越大。
         *    - speedFactor：速度越快，值越大，20m/s 以上按满分处理。
         *    - behaviorFactor：只对中危目标加成，机群优先级最高，其次是快速朝向反制区。
         */
        return switch (threatLevel) {
            case HIGH -> threatAssessmentAreaSupport.clampScore(
                    80 + (int) Math.round((0.65D * distanceFactor + 0.35D * speedFactor) * 19.0D),
                    80,
                    99
            );
            case MEDIUM -> threatAssessmentAreaSupport.clampScore(
                    50 + (int) Math.round((0.50D * distanceFactor + 0.25D * speedFactor + 0.25D * behaviorFactor) * 29.0D),
                    50,
                    79
            );
            case LOW -> threatAssessmentAreaSupport.clampScore(
                    1 + (int) Math.round((0.70D * distanceFactor + 0.30D * speedFactor) * 48.0D),
                    1,
                    49
            );
            default -> ThreatAssessmentResult.ThreatLevel.NONE.getValue();
        };
    }

    private ThreatAssessmentResult buildEmptyResult(boolean whiteList, ThreatAssessmentArea currentArea) {
        return ThreatAssessmentResult.builder()
                .whiteList(whiteList)
                .threatAssessmentArea(currentArea)
                .threatLevel(ThreatAssessmentResult.ThreatLevel.NONE)
                .threatScore(ThreatAssessmentResult.ThreatLevel.NONE.getValue())
                .build();
    }

    private boolean isUnidentifiableTarget(ThreatAssessmentArgs args) {
        return args.getTargetType() == ThreatAssessmentArgs.TargetType.TDOA && args.isImageTransmission();
    }

    private boolean isMovingTowardCountermeasure(
            ThreatAssessmentArgs current,
            List<ThreatAssessmentArgs> history,
            ThreatAssessmentAreaSupport.AreaConfigSnapshot areaSnapshot
    ) {
        if (areaSnapshot.countermeasureCentroid() == null) {
            return false;
        }

        List<ThreatAssessmentArgs> trajectory = mergeAndSortTrajectory(history, current, DIRECTION_HISTORY_WINDOW_SECONDS);
        if (trajectory.size() < 2) {
            return false;
        }

        double eastMovement = 0.0D;
        double northMovement = 0.0D;
        for (int index = 1; index < trajectory.size(); index++) {
            ThreatAssessmentArgs previous = trajectory.get(index - 1);
            ThreatAssessmentArgs next = trajectory.get(index);
            double[] movementVector = threatAssessmentAreaSupport.calculatePlanarVectorMeters(
                    previous.getLongitude(),
                    previous.getLatitude(),
                    next.getLongitude(),
                    next.getLatitude()
            );
            eastMovement += movementVector[0];
            northMovement += movementVector[1];
        }

        if (Math.hypot(eastMovement, northMovement) < 1.0D) {
            return false;
        }

        double[] toCountermeasureVector = threatAssessmentAreaSupport.calculatePlanarVectorMeters(
                current.getLongitude(),
                current.getLatitude(),
                areaSnapshot.countermeasureCentroid().left,
                areaSnapshot.countermeasureCentroid().right
        );
        if (Math.hypot(toCountermeasureVector[0], toCountermeasureVector[1]) < 1.0D) {
            return true;
        }

        double angle = threatAssessmentAreaSupport.calculateAngleBetweenVectorsDegrees(
                eastMovement,
                northMovement,
                toCountermeasureVector[0],
                toCountermeasureVector[1]
        );
        double firstDistance = threatAssessmentAreaSupport.calculateDistanceToCountermeasureCenter(trajectory.getFirst(), areaSnapshot);
        double currentDistance = threatAssessmentAreaSupport.calculateDistanceToCountermeasureCenter(current, areaSnapshot);
        return angle <= DIRECTION_ANGLE_THRESHOLD_DEGREES && currentDistance < firstDistance;
    }

    private boolean isSwarmTarget(ThreatAssessmentArgs current) {
        if (!canParticipateInSwarm(current)) {
            return false;
        }

        List<ThreatAssessmentArgs> activeTargets = getActiveTargets(current);
        List<ThreatAssessmentArgs> nearbyTargets = new ArrayList<>();
        for (ThreatAssessmentArgs target : activeTargets) {
            if (!canParticipateInSwarm(target)) {
                continue;
            }
            if (threatAssessmentAreaSupport.calculateDistance3D(current, target) <= SWARM_DISTANCE_THRESHOLD_METERS) {
                nearbyTargets.add(target);
            }
        }

        if (nearbyTargets.size() < SWARM_MIN_SIZE || nearbyTargets.size() > SWARM_MAX_SIZE) {
            return false;
        }

        for (int first = 0; first < nearbyTargets.size(); first++) {
            for (int second = first + 1; second < nearbyTargets.size(); second++) {
                if (threatAssessmentAreaSupport.calculateDistance3D(nearbyTargets.get(first), nearbyTargets.get(second)) > SWARM_DISTANCE_THRESHOLD_METERS) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean canParticipateInSwarm(ThreatAssessmentArgs args) {
        return threatAssessmentAreaSupport.isFinite(args.getLongitude())
                && threatAssessmentAreaSupport.isFinite(args.getLatitude())
                && threatAssessmentAreaSupport.isFinite(args.getAltitude())
                && !isUnidentifiableTarget(args);
    }

    private List<ThreatAssessmentArgs> getActiveTargets(ThreatAssessmentArgs current) {
        List<ThreatAssessmentArgs> activeTargets = new ArrayList<>();
        synchronized (this.uavCache) {
            Iterator<CacheObj<String, List<ThreatAssessmentArgs>>> iterator = this.uavCache.cacheObjIterator();
            while (iterator.hasNext()) {
                CacheObj<String, List<ThreatAssessmentArgs>> cacheObj = iterator.next();
                List<ThreatAssessmentArgs> trajectory = cacheObj.getValue();
                if (trajectory == null || trajectory.isEmpty()) {
                    continue;
                }
                ThreatAssessmentArgs latestTarget = trajectory.getLast();
                if (isActiveTarget(latestTarget, current.getTimestamp())) {
                    activeTargets.add(latestTarget);
                }
            }
        }

        boolean replaced = false;
        if (current.getId() != null) {
            for (int index = 0; index < activeTargets.size(); index++) {
                ThreatAssessmentArgs existing = activeTargets.get(index);
                if (Objects.equals(existing.getId(), current.getId())) {
                    activeTargets.set(index, copyArgs(current));
                    replaced = true;
                    break;
                }
            }
        }
        if (!replaced) {
            activeTargets.add(copyArgs(current));
        }
        return activeTargets;
    }

    private boolean isActiveTarget(ThreatAssessmentArgs target, LocalDateTime referenceTime) {
        if (target == null || target.getTimestamp() == null || referenceTime == null) {
            return target != null;
        }
        return Math.abs(Duration.between(target.getTimestamp(), referenceTime).getSeconds()) <= ACTIVE_TARGET_WINDOW_SECONDS;
    }

    private List<ThreatAssessmentArgs> mergeAndSortTrajectory(
            List<ThreatAssessmentArgs> history,
            ThreatAssessmentArgs current,
            long windowSeconds
    ) {
        List<ThreatAssessmentArgs> trajectory = new ArrayList<>(history.size() + 1);
        for (ThreatAssessmentArgs item : history) {
            if (item == null) {
                continue;
            }
            if (current.getTimestamp() == null || item.getTimestamp() == null) {
                trajectory.add(copyArgs(item));
                continue;
            }
            long seconds = Math.abs(Duration.between(item.getTimestamp(), current.getTimestamp()).getSeconds());
            if (seconds <= windowSeconds) {
                trajectory.add(copyArgs(item));
            }
        }
        trajectory.add(copyArgs(current));
        trajectory.sort(Comparator.comparing(
                ThreatAssessmentArgs::getTimestamp,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
        return trajectory;
    }

    private List<ThreatAssessmentArgs> getTrajectoryHistory(String id) {
        if (id == null) {
            return Collections.emptyList();
        }
        synchronized (this.uavCache) {
            List<ThreatAssessmentArgs> trajectory = this.uavCache.get(id);
            return trajectory == null ? Collections.emptyList() : new ArrayList<>(trajectory);
        }
    }

    private ThreatAssessmentArgs copyArgs(ThreatAssessmentArgs args) {
        return ThreatAssessmentArgs.builder()
                .id(args.getId())
                .targetType(args.getTargetType())
                .timestamp(args.getTimestamp())
                .imageTransmission(args.isImageTransmission())
                .whiteList(args.isWhiteList())
                .speed(args.getSpeed())
                .longitude(args.getLongitude())
                .latitude(args.getLatitude())
                .altitude(args.getAltitude())
                .build();
    }
}

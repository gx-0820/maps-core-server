package com.example.coreserver.service.countermeasure;

import com.example.coreserver.controller.OperationSseController;
import com.example.coreserver.dto.countermeasure.CountermeasureStrategyPreset;
import com.example.coreserver.dto.countermeasure.CountermeasureStrategyProfile;
import com.example.coreserver.dto.countermeasure.CountermeasureStrategyRule;
import com.example.coreserver.entity.countermeasure.CountermeasureAction;
import com.example.coreserver.entity.countermeasure.CountermeasureMode;
import com.example.coreserver.entity.threat.ThreatAssessmentResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 自动化处置后台轮次调度器。
 * 仅负责轮次编排、候选排序和策略选型，目标装载与动作执行分别委托给独立服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CountermeasureAutoTaskService {

    private static final long CONFIG_REFRESH_INTERVAL_MS = 200L;

    private final CountermeasureConfigService countermeasureConfigService;
    private final CountermeasureRoundTargetService countermeasureRoundTargetService;
    private final CountermeasureActionExecutionService countermeasureActionExecutionService;
    private final OperationSseController controller;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "countermeasure-auto-task");
        thread.setDaemon(true);
        return thread;
    });

    private final Object scheduleLock = new Object();
    private volatile boolean shutdown;
    private volatile ScheduledFuture<?> scheduledRefreshFuture;
    private volatile ScheduledFuture<?> scheduledRoundFuture;
    private volatile long scheduledRoundDelayMs = -1L;
    @Getter
    private volatile Map<String, CountermeasureRoundTargetService.AssessedTarget> currentRoundTargetCache = Map.of();
    private volatile String lastTopHighTargetId;
    private volatile CountermeasureMode observedMode;
    private final AtomicInteger lastTopHighRounds = new AtomicInteger(0);

    private static final Comparator<CountermeasureRoundTargetService.AssessedTarget> TARGET_COMPARATOR =
            Comparator.comparingInt((CountermeasureRoundTargetService.AssessedTarget target) -> severityRank(target.threatLevel())).reversed()
                    .thenComparingInt(CountermeasureRoundTargetService.AssessedTarget::threatScore).reversed()
                    .thenComparing(CountermeasureRoundTargetService.AssessedTarget::timestamp, Comparator.reverseOrder())
                    .thenComparing(CountermeasureRoundTargetService.AssessedTarget::id);

    @PostConstruct
    public void start() {
        countermeasureConfigService.ensureDefaults();
        controller.sendOperation("启动自动处置后台调度器");
        log.info("启动自动处置后台调度器");
        scheduledRefreshFuture = scheduler.scheduleWithFixedDelay(
                this::refreshScheduleSafely,
                0L,
                CONFIG_REFRESH_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }

    @PreDestroy
    public void stop() {
        shutdown = true;
        if (scheduledRefreshFuture != null) {
            scheduledRefreshFuture.cancel(true);
        }
        stopCurrentIntervention("服务关闭");
        scheduler.shutdownNow();
        controller.sendOperation("自动处置后台调度器已停止");
        log.info("自动处置后台调度器已停止");
    }

    /**
     * 立即触发一次轮次，用于模式切换或兼容接口触发。
     */
    public void triggerImmediateRound() {
        if (!shutdown) {
            controller.sendOperation("自动处置即时轮次触发");
            log.info("收到自动处置即时轮次触发请求");
            if (countermeasureConfigService.getMode() == CountermeasureMode.AUTO) {
                observedMode = CountermeasureMode.AUTO;
            }
            controller.sendOperation("即时轮次触发");
            cancelScheduledRound("即时轮次触发");
            scheduler.execute(this::runOnceSafely);
        }
    }

    /**
     * 对当前生效动作做统一收口。
     */
    public void stopCurrentIntervention(String reason) {
        cancelScheduledRound(reason);
        countermeasureActionExecutionService.stopCurrentIntervention(reason);
    }

    private void scheduleNext(long delayMs) {
        long normalizedDelayMs = Math.max(delayMs, 0L);
        synchronized (scheduleLock) {
            if (shutdown) {
                return;
            }
            if (scheduledRoundFuture != null && !scheduledRoundFuture.isDone()) {
                if (scheduledRoundDelayMs == normalizedDelayMs) {
                    return;
                }
                if (scheduledRoundFuture.cancel(false)) {
                    controller.sendOperation("自动处置轮次重排: 原计划将在 " + scheduledRoundDelayMs + "ms 后执行，现调整为 " + normalizedDelayMs + "ms");
                    log.info("自动处置轮次重排: oldDelayMs={}, newDelayMs={}", scheduledRoundDelayMs, normalizedDelayMs);
                }
                scheduledRoundFuture = null;
            }
            scheduledRoundDelayMs = normalizedDelayMs;
            scheduledRoundFuture = scheduler.schedule(() -> {
                synchronized (scheduleLock) {
                    scheduledRoundFuture = null;
                    scheduledRoundDelayMs = -1L;
                }
                runOnceSafely();
            }, normalizedDelayMs, TimeUnit.MILLISECONDS);
        }
    }

    private void cancelScheduledRound(String reason) {
        synchronized (scheduleLock) {
            if (scheduledRoundFuture == null || scheduledRoundFuture.isDone()) {
                return;
            }
            if (scheduledRoundFuture.cancel(false)) {
                controller.sendOperation("自动处置轮次取消: " + reason);
                log.info("取消已排队的自动处置轮次: reason={}", reason);
            }
            scheduledRoundFuture = null;
            scheduledRoundDelayMs = -1L;
        }
    }

    private void runOnceSafely() {
        try {
            executeRound();
        } catch (Exception e) {
            controller.sendOperation("自动处置轮次执行失败: " + e.getMessage());
            log.error("自动处置轮次执行失败: {}", e.getMessage(), e);
        } finally {
            if (!shutdown && countermeasureConfigService.getMode() == CountermeasureMode.AUTO) {
                scheduleNext(countermeasureConfigService.getScanPeriodMs());
            }
        }
    }

    private void refreshScheduleSafely() {
        try {
            refreshSchedule();
        } catch (Exception e) {
            controller.sendOperation("自动处置调度状态刷新失败: " + e.getMessage());
            log.error("自动处置调度状态刷新失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 常驻轮询配置表，负责感知 AUTO/MANUAL 切换，并将已排队轮次按最新周期重排。
     */
    private void refreshSchedule() {
        if (shutdown) {
            return;
        }
        CountermeasureMode currentMode = countermeasureConfigService.getMode();
        if (!Objects.equals(observedMode, currentMode)) {
            CountermeasureMode previousMode = observedMode;
            observedMode = currentMode;
            if (currentMode == CountermeasureMode.AUTO) {
                if (previousMode == null) {
                    controller.sendOperation("自动处置调度器已启动，当前模式=自动");
                    log.info("自动处置调度器初始化完成，当前模式=AUTO");
                    scheduleNext(0L);
                } else {
                    controller.sendOperation("自动处置模式变更为[自动]，按最新周期重建轮次");
                    log.info("自动处置模式变更为AUTO，按最新周期重建轮次");
                    scheduleNext(countermeasureConfigService.getScanPeriodMs());
                }
                return;
            }
            if (previousMode == null) {
                controller.sendOperation("自动处置调度器已启动，当前模式=人工");
                log.info("自动处置调度器初始化完成，当前模式=MANUAL");
            } else {
                controller.sendOperation("自动处置模式变更为[人工]，准备收口当前动作");
                log.info("自动处置模式变更为MANUAL，准备收口当前动作");
            }
            controller.sendOperation("配置轮询发现已切回[人工]模式");
            stopCurrentIntervention("配置轮询发现已切回人工模式");
            return;
        }
        if (currentMode == CountermeasureMode.AUTO) {
            scheduleNext(countermeasureConfigService.getScanPeriodMs());
        }
    }

    private void executeRound() {
        controller.sendOperation("自动处置轮次开始执行");
        log.info("自动处置轮次开始执行");
        countermeasureActionExecutionService.beforeNewRound();

        if (countermeasureConfigService.getMode() != CountermeasureMode.AUTO) {
            currentRoundTargetCache = Map.of();
            resetHighDominanceState();
            controller.sendOperation("当前为[人工]模式，本轮自动处置直接结束");
            log.info("当前为MANUAL模式，本轮自动处置直接结束");
            return;
        }

        long roundDurationMs = countermeasureConfigService.getScanPeriodMs();
        List<CountermeasureRoundTargetService.AssessedTarget> candidates =
                countermeasureRoundTargetService.evaluateRoundTargets(roundDurationMs);
        controller.sendOperation("自动处置候选目标统计: " + candidates.size() + "个");
        log.info("自动处置候选目标统计: candidateCount={}", candidates.size());
        currentRoundTargetCache = candidates.stream()
                .collect(Collectors.toUnmodifiableMap(CountermeasureRoundTargetService.AssessedTarget::id, target -> target, (left, right) -> left));

        if (candidates.isEmpty()) {
            resetHighDominanceState();
            return;
        }

        candidates = candidates.stream().sorted(TARGET_COMPARATOR).toList();
        updateHighDominanceState(candidates);

        CountermeasureStrategyProfile strategyProfile = countermeasureConfigService.getStrategyProfile();
        CountermeasurePlan plan = choosePlan(strategyProfile, candidates);
        if (plan == null || plan.action() == CountermeasureAction.NO_ACTION) {
            controller.sendOperation("本轮自动处置未命中可执行动作");
            log.info("本轮自动处置未命中可执行动作");
            return;
        }

        if (countermeasureConfigService.getMode() != CountermeasureMode.AUTO) {
            return;
        }

        countermeasureActionExecutionService.executePlan(plan.action(), plan.target(), plan.reason());
    }

    private void updateHighDominanceState(List<CountermeasureRoundTargetService.AssessedTarget> candidates) {
        if (candidates.isEmpty()) {
            resetHighDominanceState();
            return;
        }
        CountermeasureRoundTargetService.AssessedTarget topTarget = candidates.getFirst();
        if (topTarget.threatLevel() != ThreatAssessmentResult.ThreatLevel.HIGH) {
            resetHighDominanceState();
            return;
        }
        if (Objects.equals(topTarget.id(), lastTopHighTargetId)) {
            lastTopHighRounds.incrementAndGet();
        } else {
            lastTopHighTargetId = topTarget.id();
            lastTopHighRounds.set(1);
        }
    }

    private void resetHighDominanceState() {
        lastTopHighTargetId = null;
        lastTopHighRounds.set(0);
    }

    private CountermeasurePlan choosePlan(
            CountermeasureStrategyProfile strategyProfile,
            List<CountermeasureRoundTargetService.AssessedTarget> candidates
    ) {
        if (strategyProfile == null || strategyProfile.getActivePresetConfig() == null) {
            controller.sendOperation("自动处置策略配置缺失，无法生成本轮动作");
            log.warn("自动处置策略配置缺失，无法生成本轮动作");
            return null;
        }

        CountermeasureStrategyPreset preset = strategyProfile.getActivePresetConfig();
        controller.sendOperation("本轮自动处置策略: " + (strategyProfile.getActivePreset().trim().equals("A") ? "软防御" : "硬防御"));
        log.info("本轮自动处置策略: activePreset={}, mode={}", strategyProfile.getActivePreset(), preset.getMode());
        return switch (String.valueOf(preset.getMode()).toUpperCase()) {
            case "FIXED" -> chooseFixedPlan(preset, candidates);
            case "ADAPTIVE" -> chooseAdaptivePlan(preset, candidates);
            default -> null;
        };
    }

    private CountermeasurePlan chooseFixedPlan(
            CountermeasureStrategyPreset preset,
            List<CountermeasureRoundTargetService.AssessedTarget> candidates
    ) {
        for (ThreatAssessmentResult.ThreatLevel level : List.of(
                ThreatAssessmentResult.ThreatLevel.HIGH,
                ThreatAssessmentResult.ThreatLevel.MEDIUM,
                ThreatAssessmentResult.ThreatLevel.LOW
        )) {
            List<CountermeasureRoundTargetService.AssessedTarget> targets = candidates.stream()
                    .filter(target -> target.threatLevel() == level)
                    .sorted(TARGET_COMPARATOR)
                    .toList();
            if (targets.isEmpty()) {
                continue;
            }
            CountermeasureAction action = firstActionOrNoAction(preset.getActionsForLevel(level.name()));
            if (action == CountermeasureAction.NO_ACTION) {
                controller.sendOperation("固定策略命中等级 " + level.name() + "，但配置的动作为NO_ACTION，跳过执行");
                log.info("固定策略命中等级={}, 但动作配置为NO_ACTION", level);
                return null;
            }
            return new CountermeasurePlan(action, targets.getFirst(), "fixed:" + level.name());
        }
        return null;
    }

    private CountermeasurePlan chooseAdaptivePlan(
            CountermeasureStrategyPreset preset,
            List<CountermeasureRoundTargetService.AssessedTarget> candidates
    ) {
        CountermeasureRoundTargetService.AssessedTarget topTarget = candidates.getFirst();
        CountermeasureStrategyRule multiTargetRule = preset.getRules().get("MULTI_TARGET");
        if (multiTargetRule != null
                && multiTargetRule.isEnabled()
                && multiTargetRule.getCondition().getTargetCountGte() != null
                && candidates.size() >= multiTargetRule.getCondition().getTargetCountGte()) {
            controller.sendOperation("自适应策略命中多目标规则，当前候选目标数量 " + candidates.size() + " 满足条件要求");
            log.info("自适应策略命中MULTI_TARGET规则: candidateCount={}", candidates.size());
            return new CountermeasurePlan(firstActionOrNoAction(multiTargetRule.getActions()), topTarget, "adaptive:multi-target");
        }

        CountermeasureStrategyRule highUpgradeRule = preset.getRules().get("HIGH_DOMINANCE_UPGRADE");
        if (highUpgradeRule != null
                && highUpgradeRule.isEnabled()
                && topTarget.threatLevel() == ThreatAssessmentResult.ThreatLevel.HIGH
                && isHighDominanceSatisfied(highUpgradeRule, candidates)
                && lastTopHighRounds.get() >= defaultInt(highUpgradeRule.getCondition().getConsecutiveRoundsGte(), Integer.MAX_VALUE)) {
            controller.sendOperation("自适应策略命中高威胁升级规则，目标 " + topTarget.id() + " 已连续 " + lastTopHighRounds.get() + " 轮位列最高威胁且满足主导优势条件");
            log.info("自适应策略命中HIGH_DOMINANCE_UPGRADE规则: targetId={}", topTarget.id());
            return new CountermeasurePlan(firstActionOrNoAction(highUpgradeRule.getActions()), topTarget, "adaptive:high-upgrade");
        }

        CountermeasureAction action = firstActionOrNoAction(preset.getActionsForLevel(topTarget.threatLevel().name()));
        return action == CountermeasureAction.NO_ACTION
                ? null
                : new CountermeasurePlan(action, topTarget, "adaptive:base");
    }

    private boolean isHighDominanceSatisfied(
            CountermeasureStrategyRule highUpgradeRule,
            List<CountermeasureRoundTargetService.AssessedTarget> candidates
    ) {
        if (candidates.isEmpty()) {
            return false;
        }
        CountermeasureRoundTargetService.AssessedTarget topTarget = candidates.getFirst();
        if (topTarget.threatLevel() != ThreatAssessmentResult.ThreatLevel.HIGH) {
            return false;
        }
        int scoreGapThreshold = defaultInt(highUpgradeRule.getCondition().getScoreGapGte(), Integer.MAX_VALUE);
        int secondScore = candidates.size() > 1 ? candidates.get(1).threatScore() : 0;
        int scoreGap = topTarget.threatScore() - secondScore;
        return scoreGap >= scoreGapThreshold;
    }

    private CountermeasureAction firstActionOrNoAction(List<CountermeasureAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return CountermeasureAction.NO_ACTION;
        }
        return actions.getFirst();
    }

    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static int severityRank(ThreatAssessmentResult.ThreatLevel threatLevel) {
        return switch (threatLevel) {
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
            default -> 0;
        };
    }

    private record CountermeasurePlan(
            CountermeasureAction action,
            CountermeasureRoundTargetService.AssessedTarget target,
            String reason
    ) {
    }
}

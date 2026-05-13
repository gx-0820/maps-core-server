package com.example.coreserver.controller;

import com.example.coreserver.entity.algorithm.PredictionMetrics;
import com.example.coreserver.entity.algorithm.PredictionPoint;
import com.example.coreserver.entity.algorithm.TrackPrediction;
import com.example.coreserver.service.algorithm.AlgorithmDataProcessor;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author gaoxin
 * @description: 轨迹预测数据控制器
 */
@Slf4j
@RestController
@Api(tags = "轨迹预测接口")
@RequestMapping("/api/prediction")
public class PredictionController {

    @Autowired
    private AlgorithmDataProcessor dataProcessor;

    // SSE端点（专用于轨迹点数据推流）
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public SseEmitter streamPredictionPoints() {
        SseEmitter emitter = new SseEmitter(60_000L);
        AtomicBoolean active = new AtomicBoolean(true);

        // 单线程调度器
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        // 数据推送任务（500ms间隔）
        ScheduledFuture<?> dataTask = executor.scheduleAtFixedRate(() -> {
            if (!active.get()) {return;}

            try {
                // 获取所有目标的轨迹预测数据
                Map<Long, List<Map<String, Double>>> allTargetPoints = new HashMap<>();
                
                // 从算法处理器获取分组的预测点
                Map<Long, List<PredictionPoint>> groupedPredictions = dataProcessor.getAllPredictionPointsGroupedByTarget();

                for (Map.Entry<Long, List<PredictionPoint>> entry : groupedPredictions.entrySet()) {
                    allTargetPoints.put(entry.getKey(), formatPoints(entry.getValue()));
                }

                emitter.send(SseEmitter.event()
                        .data(allTargetPoints)
                        .id(UUID.randomUUID().toString())
                );
            } catch (IOException ex) {
                active.set(false);
            }
        }, 0, 500, TimeUnit.MILLISECONDS);

        // 生命周期回调管理
        emitter.onCompletion(() -> cleanup(executor, dataTask));
        emitter.onTimeout(() -> cleanup(executor, dataTask));

        return emitter;
    }
    
    // 获取指定目标ID的预测轨迹点
    @GetMapping(value = "/stream/{targetId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public SseEmitter streamPredictionPointsByTargetId(@PathVariable("targetId") long targetId) {
        SseEmitter emitter = new SseEmitter(60_000L);
        AtomicBoolean active = new AtomicBoolean(true);

        // 单线程调度器
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        // 数据推送任务（500ms间隔）
        ScheduledFuture<?> dataTask = executor.scheduleAtFixedRate(() -> {
            if (!active.get()) {return;}

            try {
                List<PredictionPoint> points = dataProcessor.getPredictionPointsByTargetId(targetId);
                List<Map<String, Double>> formattedPoints = formatPoints(points);
                
                emitter.send(SseEmitter.event()
                        .data(formattedPoints)
                        .id(UUID.randomUUID().toString())
                );
            } catch (IOException ex) {
                active.set(false);
            }
        }, 0, 500, TimeUnit.MILLISECONDS);

        // 生命周期回调管理
        emitter.onCompletion(() -> cleanup(executor, dataTask));
        emitter.onTimeout(() -> cleanup(executor, dataTask));

        return emitter;
    }

    // 轨迹点格式化方法
    private List<Map<String, Double>> formatPoints(List<PredictionPoint> points) {
        return Optional.ofNullable(points).orElse(Collections.emptyList()).stream()
                .map(p -> Map.of(
                        "lng", p.getLongitude(),
                        "lat", p.getLatitude(),
                        "alt", p.getAltitude()
                ))
                .collect(Collectors.toList());
    }

    // 资源清理方法
    private void cleanup(ScheduledExecutorService executor, ScheduledFuture<?> task) {
        task.cancel(true);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
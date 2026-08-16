package com.example.coreserver.service.business;

import com.example.coreserver.config.PhotoelectricVideoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoelectricVideoConvertScheduler implements SmartLifecycle {

    private final PhotoelectricVideoProperties properties;
    private final PhotoelectricVideoConvertService convertService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ScheduledExecutorService executorService;

    @Override
    public void start() {
        if (!properties.isEnabled()) {
            log.info("光电视频转换调度器已在配置中禁用");
            return;
        }
        
        if (!running.compareAndSet(false, true)) {
            log.warn("光电视频转换调度器已在运行中");
            return;
        }
        
        long fixedDelay = Math.max(properties.getScanFixedDelay(), 1_000L);
        log.info("正在启动光电视频转换调度器，扫描间隔={}ms", fixedDelay);
        
        executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "photoelectric-video-convert-scheduler");
            thread.setDaemon(true);
            return thread;
        });
        
        executorService.scheduleWithFixedDelay(this::scanSafely, 0, fixedDelay, TimeUnit.MILLISECONDS);
        log.info("光电视频转换调度器启动成功，扫描间隔: {}ms", fixedDelay);
    }

    @Override
    public void stop() {
        log.info("正在停止光电视频转换调度器...");
        running.set(false);
        if (executorService != null) {
            executorService.shutdownNow();
            log.info("光电视频转换调度器已停止");
        } else {
            log.warn("停止时执行器服务为空");
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private void scanSafely() {
        if (!running.get()) {
            log.debug("调度器未运行，跳过扫描");
            return;
        }
        
        log.debug("开始扫描待转换的视频记录");
        try {
            convertService.convertPendingRecords();
            log.debug("完成待转换视频记录的扫描");
        } catch (Exception ex) {
            log.warn("光电视频转换扫描失败", ex);
        }
    }
}

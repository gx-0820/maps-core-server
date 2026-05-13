package com.example.coreserver.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 通用数据批量保存工具类
 * @param <T> 数据类型
 */
@Slf4j
@Component
public class DataBatchUtils<T> {

    // 存储不同业务类型的队列
    private final Map<String, BatchProcessor<T>> processorMap = new HashMap<>();

    /**
     * 批量处理器内部类
     */
    private static class BatchProcessor<T> {
        private final Queue<T> bufferQueue = new ConcurrentLinkedQueue<>();
        private final Consumer<List<T>> saveCallback;
        private final int maxBatchSize;
        private final long flushInterval;

        public BatchProcessor(Consumer<List<T>> saveCallback, int maxBatchSize, long flushInterval) {
            this.saveCallback = saveCallback;
            this.maxBatchSize = maxBatchSize;
            this.flushInterval = flushInterval;
        }

        public Queue<T> getBufferQueue() {
            return bufferQueue;
        }

        public Consumer<List<T>> getSaveCallback() {
            return saveCallback;
        }

        public int getMaxBatchSize() {
            return maxBatchSize;
        }

        public long getFlushInterval() {
            return flushInterval;
        }
    }

    /**
     * 注册批量处理器
     * @param businessKey 业务标识
     * @param saveCallback 保存回调函数
     * @param maxBatchSize 最大批量大小
     * @param flushInterval 刷新间隔（毫秒）
     */
    public void register(String businessKey, Consumer<List<T>> saveCallback,
                         int maxBatchSize, long flushInterval) {
        if (processorMap.containsKey(businessKey)) {
            log.warn("业务标识 {} 已存在，将覆盖原有配置", businessKey);
        }
        processorMap.put(businessKey, new BatchProcessor<>(saveCallback, maxBatchSize, flushInterval));
        log.info("注册批量处理器成功 - 业务: {}, 批量大小: {}, 刷新间隔: {}ms",
                businessKey, maxBatchSize, flushInterval);
    }


    /**
     * 添加数据到批量队列
     * @param businessKey 业务标识
     * @param data 单条数据
     */
    public void add(String businessKey, T data) {
        if (data == null) {
            log.warn("添加的数据为空，业务标识: {}", businessKey);
            return;
        }

        BatchProcessor<T> processor = processorMap.get(businessKey);
        if (processor == null) {
            throw new IllegalArgumentException("未找到业务标识对应的处理器: " + businessKey);
        }

        processor.getBufferQueue().add(data);

        // 检查是否需要立即刷新
        if (processor.getBufferQueue().size() >= processor.getMaxBatchSize()) {
            flush(businessKey);
        }
    }

    /**
     * 批量添加数据
     * @param businessKey 业务标识
     * @param dataList 数据列表
     */
    public void addAll(String businessKey, List<T> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        BatchProcessor<T> processor = processorMap.get(businessKey);
        if (processor == null) {
            throw new IllegalArgumentException("未找到业务标识对应的处理器: " + businessKey);
        }

        processor.getBufferQueue().addAll(dataList);

        // 检查是否需要立即刷新
        if (processor.getBufferQueue().size() >= processor.getMaxBatchSize()) {
            flush(businessKey);
        }
    }

    /**
     * 刷新指定业务的数据
     * @param businessKey 业务标识
     */
    public synchronized void flush(String businessKey) {
        BatchProcessor<T> processor = processorMap.get(businessKey);
        if (processor == null) {
            log.warn("未找到业务标识对应的处理器: {}", businessKey);
            return;
        }

        Queue<T> bufferQueue = processor.getBufferQueue();
        if (bufferQueue.isEmpty()) {
            return;
        }

        List<T> batchList = new ArrayList<>();
        int batchSize = Math.min(bufferQueue.size(), processor.getMaxBatchSize());

        for (int i = 0; i < batchSize; i++) {
            T data = bufferQueue.poll();
            if (data != null) {
                batchList.add(data);
            }
        }

        if (!batchList.isEmpty()) {
            try {
                processor.getSaveCallback().accept(batchList);
                log.info("批量保存成功 - 业务: {}, 数量: {}", businessKey, batchList.size());
            } catch (Exception e) {
                log.error("批量保存失败 - 业务: {}, 数量: {}", businessKey, batchList.size(), e);
                // 保存失败，将数据重新放回队列
//                bufferQueue.addAll(batchList);
            }
        }
    }

    /**
     * 刷新所有业务的数据
     */
    public void flushAll() {
        for (String businessKey : processorMap.keySet()) {
            flush(businessKey);
        }
    }

    /**
     * 定时刷新所有业务
     */
    @Scheduled(fixedDelay = 5000)
    public void scheduledFlush() {
        for (Map.Entry<String, BatchProcessor<T>> entry : processorMap.entrySet()) {
            BatchProcessor<T> processor = entry.getValue();
            long currentTime = System.currentTimeMillis();
            // 这里简化处理，实际可以根据最后刷新时间来判断
            flush(entry.getKey());
        }
    }

    /**
     * 获取指定业务的队列大小
     * @param businessKey 业务标识
     * @return 队列大小
     */
    public int getQueueSize(String businessKey) {
        BatchProcessor<T> processor = processorMap.get(businessKey);
        return processor != null ? processor.getBufferQueue().size() : 0;
    }

    /**
     * 获取所有业务的状态
     */
    public Map<String, Integer> getAllStatus() {
        Map<String, Integer> status = new HashMap<>();
        for (Map.Entry<String, BatchProcessor<T>> entry : processorMap.entrySet()) {
            status.put(entry.getKey(), entry.getValue().getBufferQueue().size());
        }
        return status;
    }

    /**
     * 清空指定业务的数据
     * @param businessKey 业务标识
     */
    public void clear(String businessKey) {
        BatchProcessor<T> processor = processorMap.get(businessKey);
        if (processor != null) {
            processor.getBufferQueue().clear();
            log.info("清空业务数据: {}", businessKey);
        }
    }

    /**
     * 应用关闭时保存所有数据
     */
    @PreDestroy
    public void destroy() {
        log.info("应用关闭，开始保存所有剩余数据");
        flushAll();
    }
}
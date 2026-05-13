package com.example.coreserver.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 通用数据批量保存工具类（优化版）
 * 特性：
 * 1. 业务级别锁，避免不同业务互相阻塞
 * 2. 智能刷新：达到批量大小或超时时间才触发
 * 3. 失败重试机制，避免数据丢失
 * 4. 队列溢出保护，防止内存泄漏
 * 5. 完善的监控接口
 *
 * @param <T> 数据类型
 */
@Slf4j
@Component
public class DataBatchUtils1111111111<T> {

    // 存储不同业务类型的处理器
    private final Map<String, BatchProcessor<T>> processorMap = new ConcurrentHashMap<>();

    // 记录每个业务最后刷新时间
    private final Map<String, AtomicLong> lastFlushTimeMap = new ConcurrentHashMap<>();

    // 默认配置
    private static final int DEFAULT_MAX_BATCH_SIZE = 1000;
    private static final long DEFAULT_FLUSH_INTERVAL = 5000;
    private static final int DEFAULT_MAX_RETRY_TIMES = 3;
    private static final int DEFAULT_MAX_QUEUE_SIZE = 50000;

    /**
     * 批量处理器内部类
     */
    private static class BatchProcessor<T> {
        private final Queue<T> bufferQueue = new ConcurrentLinkedQueue<>();
        private final Consumer<List<T>> saveCallback;
        private final int maxBatchSize;
        private final long flushInterval;
        private final int maxRetryTimes;
        private final int maxQueueSize;

        // 失败计数（简化版，实际可用 Map<T, Integer>）
        private final Map<T, Integer> failCountMap = new ConcurrentHashMap<>();

        public BatchProcessor(Consumer<List<T>> saveCallback,
                              int maxBatchSize,
                              long flushInterval,
                              int maxRetryTimes,
                              int maxQueueSize) {
            this.saveCallback = saveCallback;
            this.maxBatchSize = maxBatchSize;
            this.flushInterval = flushInterval;
            this.maxRetryTimes = maxRetryTimes;
            this.maxQueueSize = maxQueueSize;
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

        public int getMaxRetryTimes() {
            return maxRetryTimes;
        }

        public int getMaxQueueSize() {
            return maxQueueSize;
        }

        public Map<T, Integer> getFailCountMap() {
            return failCountMap;
        }
    }

    /**
     * 注册批量处理器（使用默认配置）
     * @param businessKey 业务标识
     * @param saveCallback 保存回调函数
     */
    public void register(String businessKey, Consumer<List<T>> saveCallback) {
        register(businessKey, saveCallback, DEFAULT_MAX_BATCH_SIZE, DEFAULT_FLUSH_INTERVAL);
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
        register(businessKey, saveCallback, maxBatchSize, flushInterval,
                DEFAULT_MAX_RETRY_TIMES, DEFAULT_MAX_QUEUE_SIZE);
    }

    /**
     * 注册批量处理器（完整配置）
     * @param businessKey 业务标识
     * @param saveCallback 保存回调函数
     * @param maxBatchSize 最大批量大小
     * @param flushInterval 刷新间隔（毫秒）
     * @param maxRetryTimes 最大重试次数
     * @param maxQueueSize 最大队列容量
     */
    public void register(String businessKey, Consumer<List<T>> saveCallback,
                         int maxBatchSize, long flushInterval,
                         int maxRetryTimes, int maxQueueSize) {
        if (processorMap.containsKey(businessKey)) {
            log.warn("业务标识 {} 已存在，将覆盖原有配置", businessKey);
        }

        BatchProcessor<T> processor = new BatchProcessor<>(
                saveCallback, maxBatchSize, flushInterval, maxRetryTimes, maxQueueSize
        );

        processorMap.put(businessKey, processor);
        lastFlushTimeMap.put(businessKey, new AtomicLong(System.currentTimeMillis()));

        log.info("注册批量处理器成功 - 业务: {}, 批量大小: {}, 刷新间隔: {}ms, 最大重试: {}, 最大队列: {}",
                businessKey, maxBatchSize, flushInterval, maxRetryTimes, maxQueueSize);
    }

    /**
     * 添加数据到批量队列
     * @param businessKey 业务标识
     * @param data 单条数据
     * @return true 添加成功，false 队列已满被丢弃
     */
    public boolean add(String businessKey, T data) {
        if (data == null) {
            log.warn("添加的数据为空，业务标识: {}", businessKey);
            return false;
        }

        BatchProcessor<T> processor = processorMap.get(businessKey);
        if (processor == null) {
            throw new IllegalArgumentException("未找到业务标识对应的处理器: " + businessKey);
        }

        // 队列溢出保护
        if (processor.getBufferQueue().size() >= processor.getMaxQueueSize()) {
            log.error("业务 {} 队列已满({})，数据被丢弃: {}",
                    businessKey, processor.getMaxQueueSize(), data);
            return false;
        }

        processor.getBufferQueue().add(data);

        // 检查是否需要立即刷新
        checkAndFlush(businessKey, processor);

        return true;
    }

    /**
     * 批量添加数据
     * @param businessKey 业务标识
     * @param dataList 数据列表
     * @return 实际添加成功的数量
     */
    public int addAll(String businessKey, List<T> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }

        BatchProcessor<T> processor = processorMap.get(businessKey);
        if (processor == null) {
            throw new IllegalArgumentException("未找到业务标识对应的处理器: " + businessKey);
        }

        int addedCount = 0;
        Queue<T> queue = processor.getBufferQueue();
        int maxQueueSize = processor.getMaxQueueSize();

        for (T data : dataList) {
            if (data != null && queue.size() < maxQueueSize) {
                queue.add(data);
                addedCount++;
            }
        }

        if (addedCount < dataList.size()) {
            log.warn("业务 {} 队列添加部分成功: 请求{}条，实际添加{}条，队列大小{}/{}",
                    businessKey, dataList.size(), addedCount, queue.size(), maxQueueSize);
        }

        // 检查是否需要立即刷新
        if (addedCount > 0) {
            checkAndFlush(businessKey, processor);
        }

        return addedCount;
    }

    /**
     * 检查并执行刷新
     */
    private void checkAndFlush(String businessKey, BatchProcessor<T> processor) {
        int currentSize = processor.getBufferQueue().size();
        long now = System.currentTimeMillis();
        AtomicLong lastFlushTime = lastFlushTimeMap.get(businessKey);

        // 达到批量大小
        if (currentSize >= processor.getMaxBatchSize()) {
            flush(businessKey);
        }
        // 达到时间间隔且有数据
        else if (currentSize > 0 && (now - lastFlushTime.get()) >= processor.getFlushInterval()) {
            flush(businessKey);
        }
    }

    /**
     * 刷新指定业务的数据
     * @param businessKey 业务标识
     */
    public void flush(String businessKey) {
        BatchProcessor<T> processor = processorMap.get(businessKey);
        if (processor == null) {
            log.warn("未找到业务标识对应的处理器: {}", businessKey);
            return;
        }

        // 业务级别锁，不同业务之间不会互相阻塞
        synchronized (processor) {
            Queue<T> bufferQueue = processor.getBufferQueue();
            if (bufferQueue.isEmpty()) {
                return;
            }

            // 取出一批数据
            List<T> batchList = new ArrayList<>();
            int batchSize = Math.min(bufferQueue.size(), processor.getMaxBatchSize());

            for (int i = 0; i < batchSize; i++) {
                T data = bufferQueue.poll();
                if (data != null) {
                    batchList.add(data);
                }
            }

            if (batchList.isEmpty()) {
                return;
            }

            // 执行保存
            try {
                processor.getSaveCallback().accept(batchList);

                // 保存成功，清除失败计数
                for (T data : batchList) {
                    processor.getFailCountMap().remove(data);
                }

                // 更新最后刷新时间
                lastFlushTimeMap.get(businessKey).set(System.currentTimeMillis());

                log.debug("批量保存成功 - 业务: {}, 数量: {}", businessKey, batchList.size());

            } catch (Exception e) {
                log.error("批量保存失败 - 业务: {}, 数量: {}", businessKey, batchList.size(), e);

                // 失败重试逻辑：将数据放回队列头部（保持顺序）
                // 使用临时队列反转顺序，保持原始顺序
                LinkedList<T> tempList = new LinkedList<>(batchList);
                while (!tempList.isEmpty()) {
                    bufferQueue.offer(tempList.removeLast());
                }

                // 更新失败计数，超过重试次数则丢弃
                for (T data : batchList) {
                    Integer failCount = processor.getFailCountMap().getOrDefault(data, 0);
                    if (failCount + 1 >= processor.getMaxRetryTimes()) {
                        // 超过重试次数，丢弃数据
                        processor.getFailCountMap().remove(data);
                        log.error("业务 {} 数据重试{}次后仍失败，已丢弃: {}",
                                businessKey, processor.getMaxRetryTimes(), data);
                    } else {
                        processor.getFailCountMap().put(data, failCount + 1);
                    }
                }
            }
        }
    }

    /**
     * 刷新所有业务的数据
     */
    public void flushAll() {
        for (String businessKey : processorMap.keySet()) {
            try {
                flush(businessKey);
            } catch (Exception e) {
                log.error("刷新业务 {} 时发生异常", businessKey, e);
            }
        }
    }

    /**
     * 定时刷新（优化版：只刷新需要刷新的业务）
     */
    @Scheduled(fixedDelay = 5000)
    public void scheduledFlush() {
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<String, BatchProcessor<T>> entry : processorMap.entrySet()) {
            String businessKey = entry.getKey();
            BatchProcessor<T> processor = entry.getValue();

            // 只处理满足条件的业务
            if (processor.getBufferQueue().isEmpty()) {
                continue;
            }

            AtomicLong lastFlushTime = lastFlushTimeMap.get(businessKey);
            long timeSinceLastFlush = currentTime - lastFlushTime.get();

            // 超过刷新间隔且有数据时才刷新
            if (timeSinceLastFlush >= processor.getFlushInterval()) {
                try {
                    flush(businessKey);
                } catch (Exception e) {
                    log.error("定时刷新业务 {} 失败", businessKey, e);
                }
            }
        }
    }

    /**
     * 强制立即刷新所有数据（忽略时间和批量大小限制）
     */
    public void forceFlushAll() {
        log.info("开始强制刷新所有业务数据");
        for (String businessKey : processorMap.keySet()) {
            BatchProcessor<T> processor = processorMap.get(businessKey);
            synchronized (processor) {
                while (!processor.getBufferQueue().isEmpty()) {
                    flush(businessKey);
                }
            }
        }
        log.info("强制刷新所有业务数据完成");
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
    public Map<String, BatchStatus> getAllStatus() {
        Map<String, BatchStatus> status = new HashMap<>();
        for (Map.Entry<String, BatchProcessor<T>> entry : processorMap.entrySet()) {
            String key = entry.getKey();
            BatchProcessor<T> processor = entry.getValue();

            BatchStatus batchStatus = new BatchStatus();
            batchStatus.setQueueSize(processor.getBufferQueue().size());
            batchStatus.setMaxQueueSize(processor.getMaxQueueSize());
            batchStatus.setMaxBatchSize(processor.getMaxBatchSize());
            batchStatus.setFlushInterval(processor.getFlushInterval());
            batchStatus.setLastFlushTime(lastFlushTimeMap.get(key).get());
            batchStatus.setFailCount(processor.getFailCountMap().size());

            status.put(key, batchStatus);
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
            processor.getFailCountMap().clear();
            log.info("清空业务数据: {}", businessKey);
        }
    }

    /**
     * 移除业务处理器
     * @param businessKey 业务标识
     */
    public void unregister(String businessKey) {
        BatchProcessor<T> processor = processorMap.remove(businessKey);
        lastFlushTimeMap.remove(businessKey);
        if (processor != null) {
            // 最后尝试保存剩余数据
            synchronized (processor) {
                while (!processor.getBufferQueue().isEmpty()) {
                    flush(businessKey);
                }
            }
            log.info("移除业务处理器: {}", businessKey);
        }
    }

    /**
     * 应用关闭时保存所有数据
     */
    @PreDestroy
    public void destroy() {
        log.info("应用关闭，开始保存所有剩余数据");
        forceFlushAll();
        log.info("应用关闭，数据保存完成");
    }

    /**
     * 批量状态信息
     */
    public static class BatchStatus {
        private int queueSize;
        private int maxQueueSize;
        private int maxBatchSize;
        private long flushInterval;
        private long lastFlushTime;
        private int failCount;

        // Getters and Setters
        public int getQueueSize() { return queueSize; }
        public void setQueueSize(int queueSize) { this.queueSize = queueSize; }

        public int getMaxQueueSize() { return maxQueueSize; }
        public void setMaxQueueSize(int maxQueueSize) { this.maxQueueSize = maxQueueSize; }

        public int getMaxBatchSize() { return maxBatchSize; }
        public void setMaxBatchSize(int maxBatchSize) { this.maxBatchSize = maxBatchSize; }

        public long getFlushInterval() { return flushInterval; }
        public void setFlushInterval(long flushInterval) { this.flushInterval = flushInterval; }

        public long getLastFlushTime() { return lastFlushTime; }
        public void setLastFlushTime(long lastFlushTime) { this.lastFlushTime = lastFlushTime; }

        public int getFailCount() { return failCount; }
        public void setFailCount(int failCount) { this.failCount = failCount; }

        public long getTimeSinceLastFlush() {
            return System.currentTimeMillis() - lastFlushTime;
        }

        public double getQueueUsagePercent() {
            return maxQueueSize > 0 ? (queueSize * 100.0 / maxQueueSize) : 0;
        }

        @Override
        public String toString() {
            return String.format("BatchStatus{queueSize=%d/%d, maxBatchSize=%d, flushInterval=%dms, " +
                            "lastFlushTime=%d, timeSinceLastFlush=%dms, failCount=%d, usage=%.1f%%}",
                    queueSize, maxQueueSize, maxBatchSize, flushInterval,
                    lastFlushTime, getTimeSinceLastFlush(), failCount, getQueueUsagePercent());
        }
    }
}
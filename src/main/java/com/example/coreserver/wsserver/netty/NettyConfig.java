package com.example.coreserver.wsserver.netty;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.RuntimeUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class NettyConfig {
    private final NettyWsConfig wsServerConfig;

    public NettyConfig(NettyWsConfig wsServerConfig) {
        this.wsServerConfig = wsServerConfig;
    }


    @Bean
    public NettyDataHolder nettyDataHolder() {
//        Map<String, WsSession> userSessionMap = new ConcurrentHashMap<>();
        return new NettyDataHolder();
    }


    @Bean("newExecutorByBlockingCoefficient")
    public ThreadPoolExecutor newExecutorByBlockingCoefficient() {

        float blockingCoefficient = wsServerConfig.getBlockingCoefficient();

        int cpuCores = RuntimeUtil.getProcessorCount();
        int optimalSize = (int) (cpuCores / (1.0F - blockingCoefficient));

        // 限制最大线程数，避免资源耗尽
        int maxSize = Math.min(optimalSize, 512);
        int coreSize = Math.min(optimalSize / 2, cpuCores * 2);

        return new ThreadPoolExecutor(
                coreSize,           // 核心线程数
                maxSize,            // 最大线程数
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new NamedThreadFactory("worker-", false), // isDaemon = false（用户线程）
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

}

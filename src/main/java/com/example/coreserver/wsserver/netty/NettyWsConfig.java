package com.example.coreserver.wsserver.netty;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ws")
public class NettyWsConfig {

    private float blockingCoefficient = 0.8f;

    // ============================ws 参数配置=======================================
    private int nettyPort = 19876;
    private int bossLoopGroupThreads = 2;
    private int workerLoopGroupThreads = 16;
    private boolean useCompressionHandler = true;
    private int eventExecutorGroupThreads = 64;

}

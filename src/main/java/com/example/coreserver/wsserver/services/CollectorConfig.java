package com.example.coreserver.wsserver.services;

import com.example.coreserver.wsserver.WSConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "core")
public class CollectorConfig {

    // 采集根路径
    private String path = System.getProperty("user.dir");

}

package com.example.coreserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "device")
@Getter
@Setter
public class DeviceConfig {
    private LaserConfig laser;
    private InterferenceConfig interference;
    private DeceptionConfig deception;

    @Getter @Setter
    public static class LaserConfig {
        private String deviceId;
        private int power;
        private int duration;
    }

    @Getter @Setter
    public static class InterferenceConfig {
        private String deviceId;
        private String mode;
        private float frequency;
    }

    @Getter @Setter
    public static class DeceptionConfig {
        private String deviceId;
        private String ip;
        private String coordinates;
    }
}
package com.example.coreserver.service.device;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.example.coreserver.config.DeviceConfig;

@Component
@RequiredArgsConstructor
public class DeviceParamBuilder {
    private final DeviceConfig config;

    public String buildLaserParams() {
        return String.format("""
            {
                "deviceId": "%s",
                "power": %d,
                "duration": %d
            }
            """,
                config.getLaser().getDeviceId(),
                config.getLaser().getPower(),
                config.getLaser().getDuration()
        );
    }

    public String buildInterferenceParams() {
        return String.format("""
            {
                "deviceId": "%s",
                "mode": "%s",
                "frequency": %.1f
            }
            """,
                config.getInterference().getDeviceId(),
                config.getInterference().getMode(),
                config.getInterference().getFrequency()
        );
    }

    public String buildDeceptionParams() {
        return String.format("%s|%s",
                String.format("""
                {
                    "deviceId": "%s",
                    "ip": "%s"
                }
                """,
                        config.getDeception().getDeviceId(),
                        config.getDeception().getIp()
                ),
                String.format("""
                {
                    "coordinates": "%s"
                }
                """,
                        config.getDeception().getCoordinates()
                )
        );
    }
}
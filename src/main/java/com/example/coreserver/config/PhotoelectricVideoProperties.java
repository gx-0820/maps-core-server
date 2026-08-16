package com.example.coreserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "photoelectric-video")
public class PhotoelectricVideoProperties {

    private boolean enabled = true;

    private long scanFixedDelay = 60_000L;

    private int batchSize = 20;

    private boolean deleteSourceAfterConvert = false;

    private String ffmpegPath = "tools/ffmpeg/ffmpeg.exe";

    private long ffmpegTimeoutMinutes = 60L;
}

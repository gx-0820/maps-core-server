package com.example.coreserver.config;

import com.example.coreserver.entity.countermeasure.CountermeasureMode;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModeConfig {
    private CountermeasureMode currentMode = CountermeasureMode.MANUAL;

    public CountermeasureMode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(CountermeasureMode mode) {
        this.currentMode = mode;
    }
}
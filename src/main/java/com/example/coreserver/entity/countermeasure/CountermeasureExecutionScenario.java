package com.example.coreserver.entity.countermeasure;

import java.util.Locale;

/**
 * 自动处置执行场景。
 */
public enum CountermeasureExecutionScenario {
    DEBUG("开发场景"),
    PROD("生产场景");

    private final String description;

    CountermeasureExecutionScenario(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static CountermeasureExecutionScenario fromConfigValue(String rawValue, CountermeasureExecutionScenario defaultValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }
        for (CountermeasureExecutionScenario scenario : values()) {
            if (scenario.name().equals(rawValue.trim().toUpperCase(Locale.ROOT))) {
                return scenario;
            }
        }
        return defaultValue;
    }
}

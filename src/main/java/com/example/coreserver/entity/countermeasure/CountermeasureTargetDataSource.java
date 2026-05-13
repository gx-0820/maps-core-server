package com.example.coreserver.entity.countermeasure;

import java.util.Locale;

/**
 * 自动处置目标数据来源。
 */
public enum CountermeasureTargetDataSource {
    RADAR("雷达目标", "data_radar_target"),
    TDOA("TDOA目标", "data_tdoa_target"),
    FUSION("融合目标", "data_fusion_target");

    private final String description;
    private final String tableName;

    CountermeasureTargetDataSource(String description, String tableName) {
        this.description = description;
        this.tableName = tableName;
    }

    public String getDescription() {
        return description;
    }

    public String getTableName() {
        return tableName;
    }

    public static CountermeasureTargetDataSource fromConfigValue(String rawValue, CountermeasureTargetDataSource defaultValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }
        for (CountermeasureTargetDataSource source : values()) {
            if (source.name().equals(rawValue.trim().toUpperCase(Locale.ROOT))) {
                return source;
            }
        }
        return defaultValue;
    }
}

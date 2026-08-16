package com.example.coreserver.entity.countermeasure;

import java.util.Locale;

/**
 * 自动处置全向反制设备开启标识。
 */
public enum CountermeasureOmcFlag {
    NONE,
    ALL,
    JAMMING,
    SPOOFING;

    public static CountermeasureOmcFlag fromConfigValue(String rawValue, CountermeasureOmcFlag defaultValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }
        for (CountermeasureOmcFlag flag : values()) {
            if (flag.name().equals(rawValue.trim().toUpperCase(Locale.ROOT))) {
                return flag;
            }
        }
        return defaultValue;
    }
}

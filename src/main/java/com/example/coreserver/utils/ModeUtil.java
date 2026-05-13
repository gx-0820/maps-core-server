package com.example.coreserver.utils;

import com.example.coreserver.entity.countermeasure.CountermeasureMode;

public class ModeUtil {
    public static boolean isAutoMode(CountermeasureMode mode) {
        return CountermeasureMode.AUTO.equals(mode);
    }

    public static boolean isManualMode(CountermeasureMode mode) {
        return CountermeasureMode.MANUAL.equals(mode);
    }
}
package com.example.coreserver.dto.countermeasure;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class CountermeasureStrategyProfile {
    private String activePreset;
    private Map<String, CountermeasureStrategyPreset> presets = new LinkedHashMap<>();

    public CountermeasureStrategyPreset getActivePresetConfig() {
        if (activePreset == null) {
            return null;
        }
        return presets.get(activePreset);
    }
}

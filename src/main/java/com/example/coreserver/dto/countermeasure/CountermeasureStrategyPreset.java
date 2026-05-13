package com.example.coreserver.dto.countermeasure;

import com.example.coreserver.entity.countermeasure.CountermeasureAction;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class CountermeasureStrategyPreset {
    private String mode;
    private Map<String, List<CountermeasureAction>> actions = new LinkedHashMap<>();
    private Map<String, CountermeasureStrategyRule> rules = new LinkedHashMap<>();

    public List<CountermeasureAction> getActionsForLevel(String level) {
        return actions.getOrDefault(level, new ArrayList<>());
    }
}

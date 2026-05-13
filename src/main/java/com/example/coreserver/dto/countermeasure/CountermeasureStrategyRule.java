package com.example.coreserver.dto.countermeasure;

import com.example.coreserver.entity.countermeasure.CountermeasureAction;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CountermeasureStrategyRule {
    private boolean enabled = true;
    private Integer priority;
    private CountermeasureStrategyRuleCondition condition = new CountermeasureStrategyRuleCondition();
    private List<CountermeasureAction> actions = new ArrayList<>();
}

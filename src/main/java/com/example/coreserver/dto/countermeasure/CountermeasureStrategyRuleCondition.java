package com.example.coreserver.dto.countermeasure;

import lombok.Data;

@Data
public class CountermeasureStrategyRuleCondition {
    private Integer targetCountGte;
    private String threatLevel;
    private Integer scoreGapGte;
    private Integer consecutiveRoundsGte;
}

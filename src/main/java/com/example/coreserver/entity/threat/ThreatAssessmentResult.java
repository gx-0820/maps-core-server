package com.example.coreserver.entity.threat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author : [wangminan]
 * @description : 威胁评估结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatAssessmentResult {

    /**
     * 威胁评估等级
     */
    private ThreatLevel threatLevel;

    /**
     * 威胁评估区域
     */
    private ThreatAssessmentArea threatAssessmentArea;

    /**
     * 威胁评估分数
     */
    @Builder.Default
    private Integer threatScore = ThreatLevel.NONE.getValue();

    /**
     * 是否白名单
     */
    private boolean whiteList;

    public enum ThreatLevel {
        HIGH(1),
        MEDIUM(2),
        LOW(3),
        NONE(-1);

        @Getter
        private final int value;
        ThreatLevel(int value) {
            this.value = value;
        }

        public ThreatLevel fromValue(int value) {
            for (ThreatLevel level : ThreatLevel.values()) {
                if (level.value == value) {
                    return level;
                }
            }
            return null;
        }

    }

    public enum ThreatAssessmentArea {
        COUNTERMEASURE("反制区"),
        WARNING("预警区"),
        DETECTION("探测区"),
        OUTSIDE("三区外");

        @Getter
        private final String areaName;

        ThreatAssessmentArea(String areaName) {
            this.areaName = areaName;
        }
    }
}

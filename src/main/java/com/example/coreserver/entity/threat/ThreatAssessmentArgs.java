package com.example.coreserver.entity.threat;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author : [wangminan]
 * @description : 威胁评估用入参
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatAssessmentArgs {

    /**
     * 我不管你用雷达Id还是什么别的Id 但总之是要有Id 这是我在本地做机群匹配LRUCache的key
     */
    private String id;

    /**
     * 目标类型
     */
    @Builder.Default
    private TargetType targetType = TargetType.RADAR;

    /**
     * 时间戳
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 是否图传
     */
    @Builder.Default
    private boolean imageTransmission = false;

    /**
     * 是否白名单 只有TDOA上报
     */
    @Builder.Default
    private boolean whiteList = false;

    /**
     * 目标速度
     */
    private double speed;

    /**
     * 经度，威胁评估统一按 WGS84 坐标系处理。
     */
    private double longitude;

    /**
     * 纬度，威胁评估统一按 WGS84 坐标系处理。
     */
    private double latitude;

    /**
     * 高度
     */
    private double altitude;

    public enum TargetType {
        // 雷达
        RADAR,
        // TODA
        TDOA,
        // 融合
        FUSION,
        @JsonEnumDefaultValue
        OTHER;
    }
}

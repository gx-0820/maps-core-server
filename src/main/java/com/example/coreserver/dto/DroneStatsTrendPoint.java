package com.example.coreserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DroneStatsTrendPoint {
    /**
     * 统计时间点（每天一条）
     */
    private LocalDate statTime;

    /**
     * 雷达监控无人机目标个数
     */
    private Integer radarTargetCount;

    /**
     * TDOA监控无人机目标个数
     */
    private Integer tdoaTargetCount;

    /**
     * 融合后无人机目标个数
     */
    private Integer fusionTargetCount;

    /**
     * 雷达监控非法无人机个数
     */
    private Integer radarIllegalCount;

    /**
     * TDOA监控非法无人机个数
     */
    private Integer tdoaIllegalCount;

    /**
     * 融合后非法无人机目标个数
     */
    private Integer fusionIllegalCount;

    /**
     * 应处置数量
     */
    private Integer needDisposeCount;

    /**
     * 有效处置数量
     */
    private Integer effectiveDisposeCount;
}

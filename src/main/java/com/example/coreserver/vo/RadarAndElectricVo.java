package com.example.coreserver.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RadarAndElectricVo {
    private String deviceId;      // 设备ID
    private int targetDistance;    // 目标距离
    private double targetAzimuth;  // 目标方位角
    private double targetElevation;// 目标俯仰角
    private int targetDistanceCorrection;// 目标距离修正量
    private double targetAzimuthCorrection;  // 目标方位角修正量
    private double targetElevationCorrection;// 目标俯仰角修正量

    // 点击列表调用
    private String dataType;
    /**
     * 目标纬度坐标
     */
    private BigDecimal lat;

    /**
     * 目标经度坐标
     */
    private int lon;
    /**
     * 目标高度
     */
    private BigDecimal altitude;
} 
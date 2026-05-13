package com.example.coreserver.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GuidanceControlVo {
    private String deviceId;      // 设备ID
    private String targetId; // 目标id  雷达targetId 融合数据id tdoa:sensorId
    private String targetNo; // 目标编号

    // 点击列表调用
    private String dataType;

    private String batchId;
//    /**
//     * 目标纬度坐标
//     */
//    private BigDecimal lat;
//
//    /**
//     * 目标经度坐标
//     */
//    private BigDecimal lon;
//    /**
//     * 目标高度
//     */
//    private BigDecimal altitude;
} 
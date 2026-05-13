package com.example.coreserver.entity.algorithm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DataFusion {

    @JsonProperty(value = "targetId")
    private int id;       // targetId

    private double[] position;  // [经度，纬度，高度]
    private double velocity;    // 米每秒
    private double azimuth;     // 方位角
    private String type;        // 类型
    private String name;        // 名称
    private double distance;    // 距离
    private double pitch;       // 俯仰角
    private int threadLevel; // 威胁等级
    private double panAngle;    // 水平角度
    private double tiltAngle;   // 倾斜角度
    private double zoomLevel;   // 缩放级别
    private String color;       // 颜色
}
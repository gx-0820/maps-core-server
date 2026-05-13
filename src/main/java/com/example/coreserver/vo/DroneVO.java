package com.example.coreserver.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Builder
public class DroneVO {

    // id
    private int id;

    // 核心定位信息
    private double longitude;     // 经度（从 position[0] 提取）
    private double latitude;       // 纬度（从 position[1] 提取）
    private double altitude;       // 高度（从 position[2] 提取）

    // 动态参数
    private double velocity;       // 速度（米/秒）
    private double azimuth;        // 方位角（单位：度）
    private double pitch;          // 俯仰角（新增字段）

    // 目标属性
    private String type;           // 目标类型（如 "Dji FRV"）
    private String name;           // 目标名称（如 "大疆"）
    private String color;          // 渲染颜色（如 "FFFF00"）
    private int threatLevel;    // 威胁等级（"123"）

    // 观测参数
    private double distance;       // 距离（米）
    private double panAngle;       // 水平角（新增字段）
    private double tiltAngle;      // 倾斜角（新增字段）
    private double zoomLevel;      // 缩放级别（新增字段）

    // 系统信息
    private LocalDateTime createTime; //创建时间
    private String lastUpdateTime;// 最后更新时间（格式：yyyy-MM-dd HH:mm:ss）

}

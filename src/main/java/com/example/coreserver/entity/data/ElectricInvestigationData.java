package com.example.coreserver.entity.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ElectricInvestigationData {
    @Schema(description = "飞行物高度")
    private double altitude;

    @Schema(description = "飞行物方位角")
    private double azimuth;

    @Schema(description = "飞行物颜色，RGB标准 #开头 尽量不同的颜色 颜色还需要用于标识轨迹")
    private String color;

    @Schema(description = "内部攻击通道")
    private double distance;

    @Schema(description = "飞行物id")
    private String id;

    @Schema(description = "飞行物纬度")
    private String lat;

    @Schema(description = "飞行物经度")
    private String lng;

    @Schema(description = "飞行物名称")
    private String name;

    @Schema(description = "飞行物轨迹")
    private List<List<String>> path;

    @Schema(description = "飞行物俯仰角")
    private double pitchAngle;

    @Schema(description = "飞行物速度")
    private String speed;

    @Schema(description = "行物威胁等级，字符串常量 Low/Middle/High")
    private String threadLevel;

    @Schema(description = "类型 符串常量 UAV")
    private String type;

    @Schema(description = "飞行物更新时间")
    @JsonFormat(timezone = "GMT+8", pattern = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalDateTime updateTime;
} 
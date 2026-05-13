package com.example.coreserver.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RadarTarget {
    private float xCoordinate;
    private float yCoordinate;
    private float zCoordinate;
    private float xSpeed;
    private float ySpeed;
    private float zSpeed;

    private int targetId;

    private double snr; // 信噪比
    private double rcs; // 反射截面积
    private int targetType;
    private int selectionFlag;

    // 计算后的数据

    private double range; // 轴向距离

    private double azimuth1; // 方位角 (以转台0度为基准)

    private double azimuth2; // 方位角 (以正北0度为基准)

    private double pitch; // 俯仰角

    private double speed; // 速度大小

    private double targetLat; //飞行物纬度

    private double targetLon; //飞行物经度

    private double altitude; //飞行物高度

    private int statusNum;

    private int isDelete;

    @JsonIgnore
    private LocalDateTime createTime; //创建时间

    @Override
    public String toString() {
        return "RadarTarget{" +
                "xCoordinate=" + xCoordinate +
                ", yCoordinate=" + yCoordinate +
                ", zCoordinate=" + zCoordinate +
                ", xSpeed=" + xSpeed +
                ", ySpeed=" + ySpeed +
                ", zSpeed=" + zSpeed +
                ", targetId=" + targetId +
                ", snr=" + snr +
                ", rcs=" + rcs +
                ", targetType=" + targetType +
                ", selectionFlag=" + selectionFlag +
                ", range=" + range +
                ", azimuth1=" + azimuth1 +
                ", azimuth2=" + azimuth2 +
                ", pitch=" + pitch +
                ", speed=" + speed +
                ", targetLat=" + targetLat +
                ", targetLon=" + targetLon +
                ", altitude=" + altitude +
                ", statusNum=" + statusNum +
                ", isDelete=" + isDelete +
                ", createTime=" + createTime +
                '}';
    }
}

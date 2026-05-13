package com.example.coreserver.entity.data;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
/**
 * @author : [dou427]
 * @description : 光电设备属性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoelectricData {
    @Schema(description = "设备ID")
    private String deviceId;                // 设备ID

    @Schema(description = "时间戳")
    private LocalDateTime timestamp;        // 时间戳

    // RTSP流地址
    @Schema(description = "可见光RTSP地址")
    private String visibleLightUrl;         // 可见光RTSP地址

    // 设备状态
    @Schema(description = "可见光状态")
    private boolean visibleLightStatus;     // 可见光状态

    // 视频参数
    @Schema(description = "视频宽度")
    private int width;                      // 视频宽度

    @Schema(description = "视频高度")
    private int height;                     // 视频高度

    @Schema(description = "帧率")
    private int fps;                        // 帧率

    @Schema(description = "编码格式")
    private String codec;                   // 编码格式

    @Schema(description = "分辨率")
    private String resolution;              // 分辨率

    // 流类型
    @Schema(description = "当前流类型(visible/infrared)")
    private String currentStreamType;       // 当前流类型(visible/infrared)

    // 帧计数
    @Schema(description = "可见光帧计数")
    private long visibleLightFrameCount;    // 可见光帧计数


    // 帧数据（如果需要）
    @Schema(description = "可见光帧数据")
    private byte[] visibleLightFrame;       // 可见光帧数据

    // 测量数据
    @Schema(description = "激光测距值(米)")
    private double laserDistance;           // 激光测距值，单位：米

    @Schema(description = "方位角(0-360度)")
    private double azimuthAngle;           // 方位角，范围：0-360度

    @Schema(description = "俯仰角(-90到+90度)")
    private double pitchAngle;             // 俯仰角，范围：-90到+90度

    @Schema(description = "方位角速度(度/秒)")
    private double azimuthSpeed;           // 方位角速度，单位：度/秒

    @Schema(description = "俯仰角速度(度/秒)")
    private double pitchSpeed;             // 俯仰角速度，单位：度/秒

    @Schema(description = "方位跟踪误差(度)")
    private double azimuthError;           // 方位跟踪误差，单位：度

    @Schema(description = "俯仰跟踪误差(度)")
    private double pitchError;             // 俯仰跟踪误差，单位：度

    // 设备状态
    @Schema(description = "自动模式状态")
    private String autoMode;              // true=自动模式，false=手动模式

    @Schema(description = "伺服模式(TRACK/SCAN/MANUAL)")
    private String servoMode;              // 伺服模式

    @Schema(description = "跟踪状态(SEARCHING/TRACKING/LOST)")
    private String trackingStatus;         // 跟踪状态

    @Schema(description = "跟踪通道(红外/电视)")
    private String trackingChannel;        // 跟踪通道

    @Schema(description = "伺服电源状态")
    private String servoPowerStatus;      // 伺服电源状态

    @Schema(description = "伺服就绪状态")
    private String servoReadyStatus;      // 伺服就绪状态

    @Schema(description = "命令执行状态")
    private String commandExecuteStatus;    // 命令执行状态

    @Schema(description = "命令响应消息")
    private String commandResponseMessage;   // 命令响应消息

    private String isCorrelation;      // 跟踪算法
    private String isPolarityBlack;    // 目标极性
    private String serverPowerStatus;  // 伺服电源状态

    private double laserEnergy;  // 激光能量

    // Getters and Setters
    public String getIsCorrelation() { return isCorrelation; }
    public void setIsCorrelation(String isCorrelation) { this.isCorrelation = isCorrelation; }

    public String getIsPolarityBlack() { return isPolarityBlack; }
    public void setIsPolarityBlack(String isPolarityBlack) { this.isPolarityBlack = isPolarityBlack; }

    public String getServerPowerStatus() { return serverPowerStatus; }
    public void setServerPowerStatus(String serverPowerStatus) { this.serverPowerStatus = serverPowerStatus; }

    public double getLaserEnergy() { return laserEnergy; }
    public void setLaserEnergy(double laserEnergy) { this.laserEnergy = laserEnergy; }


    public String isAutoMode() {
        return autoMode;
    }

    public String isServoPowerStatus() {
        return servoPowerStatus;
    }

    public String isServoReadyStatus() {
        return servoReadyStatus;
    }

    public String isCommandExecuteStatus() {
        return commandExecuteStatus;
    }
}

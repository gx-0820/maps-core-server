package com.example.coreserver.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

/**
 * @TableName data_ofd
 */
@TableName(value ="data_ofd")
public class DataOfd {
    private Long id;

    private Date timestamp;

    private String deviceId;

    private String visibleLightUrl;

    private Integer visibleLightStatus;

    private Integer width;

    private Integer height;

    private Integer fps;

    private String codec;

    private String resolution;

    private String currentStreamType;

    private Long visibleLightFrameCount;

    private Double laserDistance;

    private Double azimuthAngle;

    private Double pitchAngle;

    private Double azimuthSpeed;

    private Double pitchSpeed;

    private Double azimuthError;

    private Double pitchError;

    private String autoMode;

    private String servoMode;

    private String trackingStatus;

    private String trackingChannel;

    private String servoPowerStatus;

    private String servoReadyStatus;

    private String commandExecuteStatus;

    private String commandResponseMessage;

    private String isCorrelation;

    private String isPolarityBlack;

    private String serverPowerStatus;

    private Double laserEnergy;

    private Date createTime;

    private Date updateTime;

    private byte[] visibleLightFrame;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getVisibleLightUrl() {
        return visibleLightUrl;
    }

    public void setVisibleLightUrl(String visibleLightUrl) {
        this.visibleLightUrl = visibleLightUrl;
    }

    public Integer getVisibleLightStatus() {
        return visibleLightStatus;
    }

    public void setVisibleLightStatus(Integer visibleLightStatus) {
        this.visibleLightStatus = visibleLightStatus;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getFps() {
        return fps;
    }

    public void setFps(Integer fps) {
        this.fps = fps;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getCurrentStreamType() {
        return currentStreamType;
    }

    public void setCurrentStreamType(String currentStreamType) {
        this.currentStreamType = currentStreamType;
    }

    public Long getVisibleLightFrameCount() {
        return visibleLightFrameCount;
    }

    public void setVisibleLightFrameCount(Long visibleLightFrameCount) {
        this.visibleLightFrameCount = visibleLightFrameCount;
    }

    public Double getLaserDistance() {
        return laserDistance;
    }

    public void setLaserDistance(Double laserDistance) {
        this.laserDistance = laserDistance;
    }

    public Double getAzimuthAngle() {
        return azimuthAngle;
    }

    public void setAzimuthAngle(Double azimuthAngle) {
        this.azimuthAngle = azimuthAngle;
    }

    public Double getPitchAngle() {
        return pitchAngle;
    }

    public void setPitchAngle(Double pitchAngle) {
        this.pitchAngle = pitchAngle;
    }

    public Double getAzimuthSpeed() {
        return azimuthSpeed;
    }

    public void setAzimuthSpeed(Double azimuthSpeed) {
        this.azimuthSpeed = azimuthSpeed;
    }

    public Double getPitchSpeed() {
        return pitchSpeed;
    }

    public void setPitchSpeed(Double pitchSpeed) {
        this.pitchSpeed = pitchSpeed;
    }

    public Double getAzimuthError() {
        return azimuthError;
    }

    public void setAzimuthError(Double azimuthError) {
        this.azimuthError = azimuthError;
    }

    public Double getPitchError() {
        return pitchError;
    }

    public void setPitchError(Double pitchError) {
        this.pitchError = pitchError;
    }

    public String getAutoMode() {
        return autoMode;
    }

    public void setAutoMode(String autoMode) {
        this.autoMode = autoMode;
    }

    public String getServoMode() {
        return servoMode;
    }

    public void setServoMode(String servoMode) {
        this.servoMode = servoMode;
    }

    public String getTrackingStatus() {
        return trackingStatus;
    }

    public void setTrackingStatus(String trackingStatus) {
        this.trackingStatus = trackingStatus;
    }

    public String getTrackingChannel() {
        return trackingChannel;
    }

    public void setTrackingChannel(String trackingChannel) {
        this.trackingChannel = trackingChannel;
    }

    public String getServoPowerStatus() {
        return servoPowerStatus;
    }

    public void setServoPowerStatus(String servoPowerStatus) {
        this.servoPowerStatus = servoPowerStatus;
    }

    public String getServoReadyStatus() {
        return servoReadyStatus;
    }

    public void setServoReadyStatus(String servoReadyStatus) {
        this.servoReadyStatus = servoReadyStatus;
    }

    public String getCommandExecuteStatus() {
        return commandExecuteStatus;
    }

    public void setCommandExecuteStatus(String commandExecuteStatus) {
        this.commandExecuteStatus = commandExecuteStatus;
    }

    public String getCommandResponseMessage() {
        return commandResponseMessage;
    }

    public void setCommandResponseMessage(String commandResponseMessage) {
        this.commandResponseMessage = commandResponseMessage;
    }

    public String getIsCorrelation() {
        return isCorrelation;
    }

    public void setIsCorrelation(String isCorrelation) {
        this.isCorrelation = isCorrelation;
    }

    public String getIsPolarityBlack() {
        return isPolarityBlack;
    }

    public void setIsPolarityBlack(String isPolarityBlack) {
        this.isPolarityBlack = isPolarityBlack;
    }

    public String getServerPowerStatus() {
        return serverPowerStatus;
    }

    public void setServerPowerStatus(String serverPowerStatus) {
        this.serverPowerStatus = serverPowerStatus;
    }

    public Double getLaserEnergy() {
        return laserEnergy;
    }

    public void setLaserEnergy(Double laserEnergy) {
        this.laserEnergy = laserEnergy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public byte[] getVisibleLightFrame() {
        return visibleLightFrame;
    }

    public void setVisibleLightFrame(byte[] visibleLightFrame) {
        this.visibleLightFrame = visibleLightFrame;
    }
}
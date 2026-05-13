package com.example.coreserver.dto;

import jakarta.validation.constraints.NotBlank;

//public class CountermeasureRequest {
//    @NotBlank(message = "威胁信息不能为空")
//    private String threatInfo;
//
//    public String getThreatInfo() {
//        return threatInfo;
//    }
//
//    public void setThreatInfo(String threatInfo) {
//        this.threatInfo = threatInfo;
//    }
//}

public class CountermeasureRequest {
    @NotBlank(message = "威胁信息不能为空")
    private String threatInfo;

    // 激光设备（如需从请求指定设备及动作）
    private String laserDeviceId;

    // 诱骗设备相关参数
    private String deceptionDeviceId;
    private String deceptionIp;
    private String deceptionCoordinates;

    // 无线自动攻击相关参数
    private String uavDeviceId;
    private Boolean autoAttackCancel;

    public String getThreatInfo() {
        return threatInfo;
    }

    public void setThreatInfo(String threatInfo) {
        this.threatInfo = threatInfo;
    }

    public String getLaserDeviceId() {
        return laserDeviceId;
    }

    public void setLaserDeviceId(String laserDeviceId) {
        this.laserDeviceId = laserDeviceId;
    }

    public String getDeceptionDeviceId() {
        return deceptionDeviceId;
    }

    public void setDeceptionDeviceId(String deceptionDeviceId) {
        this.deceptionDeviceId = deceptionDeviceId;
    }

    public String getDeceptionIp() {
        return deceptionIp;
    }

    public void setDeceptionIp(String deceptionIp) {
        this.deceptionIp = deceptionIp;
    }

    public String getDeceptionCoordinates() {
        return deceptionCoordinates;
    }

    public void setDeceptionCoordinates(String deceptionCoordinates) {
        this.deceptionCoordinates = deceptionCoordinates;
    }

    public String getUavDeviceId() {
        return uavDeviceId;
    }

    public void setUavDeviceId(String uavDeviceId) {
        this.uavDeviceId = uavDeviceId;
    }

    public Boolean getAutoAttackCancel() {
        return autoAttackCancel;
    }

    public void setAutoAttackCancel(Boolean autoAttackCancel) {
        this.autoAttackCancel = autoAttackCancel;
    }
}
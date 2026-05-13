package com.example.coreserver.dto;

import jakarta.validation.constraints.*;

public class CreateGeofenceRequest {
    @NotBlank(message = "名称不能为空")
    private String name;

    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
    private Double coreLongitude;

    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
    private Double coreLatitude;

    @NotNull @Positive(message = "核心半径必须大于0")
    private Double coreRadius;

    @NotNull @Positive(message = "缓冲半径必须大于0")
    private Double bufferRadius;

    @NotNull @Positive(message = "告警半径必须大于0")
    private Double alertRadius;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCoreLongitude() {
        return coreLongitude;
    }

    public void setCoreLongitude(Double coreLongitude) {
        this.coreLongitude = coreLongitude;
    }

    public Double getCoreLatitude() {
        return coreLatitude;
    }

    public void setCoreLatitude(Double coreLatitude) {
        this.coreLatitude = coreLatitude;
    }

    public Double getCoreRadius() {
        return coreRadius;
    }

    public void setCoreRadius(Double coreRadius) {
        this.coreRadius = coreRadius;
    }

    public Double getBufferRadius() {
        return bufferRadius;
    }

    public void setBufferRadius(Double bufferRadius) {
        this.bufferRadius = bufferRadius;
    }

    public Double getAlertRadius() {
        return alertRadius;
    }

    public void setAlertRadius(Double alertRadius) {
        this.alertRadius = alertRadius;
    }
}
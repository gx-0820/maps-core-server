package com.example.coreserver.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class UpdateGeofenceRequest {
    @Size(min = 1, max = 100)
    private String name;

    @DecimalMin("-180.0") @DecimalMax("180.0")
    private Double coreLongitude;

    @DecimalMin("-90.0") @DecimalMax("90.0")
    private Double coreLatitude;

    @Positive private Double coreRadius;
    @Positive private Double bufferRadius;
    @Positive private Double alertRadius;

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
package com.example.coreserver.service.algorithm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DataFusionVo {

    @JsonProperty("target_seq")
    private String targetSeq;

    @JsonProperty("target_type")
    private String targetType;

    @JsonProperty("uav_model")
    private String uavModel;

    @JsonProperty("azimuth")
    private BigDecimal azimuth;

    @JsonProperty("pitch")
    private BigDecimal pitch;

    @JsonProperty("range")
    private BigDecimal range;

    @JsonProperty("target_lon")
    private BigDecimal targetLon;

    @JsonProperty("target_lat")
    private BigDecimal targetLat;

    @JsonProperty("altitude")
    private BigDecimal altitude;

    @JsonProperty("speed")
    private BigDecimal speed;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("radar_targetd_id")
    private String radarTargetdId;

    @JsonProperty("tdoa_targetd_id")
    private String tdoaTargetdId;
}

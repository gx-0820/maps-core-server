package com.example.coreserver.entity.algorithm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PredictionMetrics {
    private int frame;
    @JsonProperty("lat_mae") private double latitudeMAE;
    @JsonProperty("lon_mae") private double longitudeMAE;
    @JsonProperty("alt_mae") private double altitudeMAE;
    @JsonProperty("total_mae") private double totalMAE;
}

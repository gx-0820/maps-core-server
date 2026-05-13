package com.example.coreserver.entity.algorithm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackPrediction {
    @JsonProperty(value = "targetid")
    private long targetId;

    @JsonProperty(value = "predictions")
    private List<PredictionPoint> predictions = new ArrayList<>();
}
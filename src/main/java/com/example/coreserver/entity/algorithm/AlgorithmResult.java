package com.example.coreserver.entity.algorithm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlgorithmResult {
//    @JsonProperty(value = "ObjectDetection")
//    @JsonDeserialize(as = ArrayList.class)
//    private List<ObjectDetection> objectDetection;
//
//    @JsonProperty(value = "TrackPrediction")
//    private List<TrackPrediction> trackPrediction;

    @JsonProperty(value = "DataFusion")
    private List<DataFusion> dataFusion;

    @JsonProperty(value = "GeoPositionValidation")
    private List<GeoPositionValidator> geoPositionValidator;
}
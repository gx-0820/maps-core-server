package com.example.coreserver.entity.algorithm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ObjectDetection {
    @JsonProperty(value = "targets")
    private List<DetectionTarget> targets;
}
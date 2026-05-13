package com.example.coreserver.service.algorithm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DataFusionResponse {

    @JsonProperty("DataFusion")
    private List<DataFusionVo> dataFusion;

}

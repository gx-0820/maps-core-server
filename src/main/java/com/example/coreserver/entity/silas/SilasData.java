/**
  * Copyright 2025 bejson.com
  */
package com.example.coreserver.entity.silas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 * Auto-generated: 2025-01-08 14:55:43
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
@Builder
public class SilasData {

    private Sensor sensor;
    @JsonProperty("CRS")
    private String CRS;
    private HeightDesc heightDesc;
    @Singular
    @JsonProperty("perceivedStatus")
    private List<PerceivedStatus> perceivedStatuses;

}

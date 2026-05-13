/**
  * Copyright 2025 bejson.com
  */
package com.example.coreserver.entity.silas;

import lombok.Builder;
import lombok.Data;

/**
 * Auto-generated: 2025-01-08 14:55:43
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
@Builder
public class Sensor {

    private String sensorManufacturerID;
    private String sensorSN;
    private SensorOperatorInfo sensorOperatorInfo;
    private SensorLocation sensorLocation;
    private SensorPosture sensorPosture;
    private SensorExtra sensorExtra;

}

package com.example.coreserver.entity.silas;

import lombok.Builder;
import lombok.Data;

/**
 * @author : [Lenovo]
 * @description : [一句话描述该类的功能]
 */
@Data
@Builder
public class SensorPosition {
    private String positionBase;
    private Double range;
    private Double azimuth;
    private Double elevation;
}

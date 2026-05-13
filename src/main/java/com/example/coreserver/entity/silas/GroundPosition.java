package com.example.coreserver.entity.silas;

import lombok.Builder;
import lombok.Data;

/**
 * @author : [Lenovo]
 * @description : [一句话描述该类的功能]
 */
@Data
@Builder
public class GroundPosition {
    private String positionBase;
    private Double longitude;
    private Double latitude;
    private Double height;
    private Double deltaLongitude;
    private Double deltaLatitude;
    private Double deltaHeight;
}

package com.example.coreserver.entity.silas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author : [Lenovo]
 * @description : [一句话描述该类的功能]
 */
@Data
@Builder
public class Aircraft {
    //Normal：正常
    // Deregister：注销
    // NoRecord：无记录
    private String aircraftState;
    // Fixed-wingAircraft 固定翼
    // Rotorcraft旋翼航空器
    // Multi-rotorAircraft多桨或多轴航空器
    // Compound-wingAircraft复合翼航空器
    // Airship飞艇
    // Other其他
    private String aircraftType;
    // MicroUAV微型无人驾驶航空器
    // LightUAV轻型无人驾驶航空器
    // SmallUAV小型无人驾驶航空器
    // MediumUAV中型无人驾驶航空器
    // LargeUAV大型无人驾驶航空器
    private String aircraftCategory;
    private String aircraftName;
    private String aircraftModel;
    private String manufacturerName;
    private Double aircraftEmptyWeight;
    private Double aircraftEmptyWeightWithBattery;
    private Double actualPayloadWeight;
    private Double maxTakeoffWeight;
    @JsonProperty("SN")
    private String SN;
}

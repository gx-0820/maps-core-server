package com.example.coreserver.entity.algorithm;

import lombok.Data;

@Data
public class GeoPositionValidator {
    private double[] position;    // [经度, 纬度, 高度]
    private int warningLevel;     // 警告级别：1-最内圈，2-二级，3-三级
}
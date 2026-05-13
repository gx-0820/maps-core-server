package com.example.coreserver.entity.algorithm;

import lombok.Data;

@Data
public class DetectionTarget {
    private int left;
    private int top;
    private int width;
    private int height;
}
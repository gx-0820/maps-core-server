package com.example.coreserver.dto.countermeasure;

import com.example.coreserver.entity.countermeasure.CountermeasureDeviceDirection;

public record CountermeasureModeOptionDevice(
        Long id,
        String type,
        String brand,
        String model,
        String collectFlag,
        String name,
        String location,
        String coverage,
        CountermeasureDeviceDirection direction,
        boolean executable,
        String executableReason
) {
}

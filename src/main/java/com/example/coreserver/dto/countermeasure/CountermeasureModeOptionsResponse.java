package com.example.coreserver.dto.countermeasure;

import com.example.coreserver.entity.countermeasure.CountermeasureMode;
import com.example.coreserver.entity.countermeasure.CountermeasureOmcFlag;

import java.util.List;

public record CountermeasureModeOptionsResponse(
        CountermeasureMode mode,
        String location,
        CountermeasureOmcFlag currentOmcFlag,
        List<CountermeasureModeOptionDevice> devices,
        List<CountermeasureOmcFlag> supportedOmcFlags
) {
}

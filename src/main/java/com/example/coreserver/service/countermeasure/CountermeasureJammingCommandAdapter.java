package com.example.coreserver.service.countermeasure;

import java.util.List;

public interface CountermeasureJammingCommandAdapter {

    String adapterId();

    CountermeasureJammingPlan buildPlan(
            CountermeasureDeviceDirectoryService.CountermeasureRuntimeDevice device,
            List<String> configuredFrequencies
    );
}

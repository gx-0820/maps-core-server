package com.example.coreserver.service.countermeasure;

import java.util.List;

public record CountermeasureJammingPlan(
        List<CountermeasureNettyCommand> startCommands,
        List<CountermeasureNettyCommand> stopCommands,
        String description
) {
}

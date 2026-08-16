package com.example.coreserver.service.countermeasure;

import java.util.Map;

public record CountermeasureNettyCommand(
        String deviceCode,
        String command,
        Map<String, Object> args
) {
}

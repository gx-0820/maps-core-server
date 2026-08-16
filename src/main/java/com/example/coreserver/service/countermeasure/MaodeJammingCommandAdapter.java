package com.example.coreserver.service.countermeasure;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class MaodeJammingCommandAdapter implements CountermeasureJammingCommandAdapter {

    @Override
    public String adapterId() {
        return CountermeasureDeviceDirectoryService.ADAPTER_MAODE_OMNI;
    }

    @Override
    public CountermeasureJammingPlan buildPlan(
            CountermeasureDeviceDirectoryService.CountermeasureRuntimeDevice device,
            List<String> configuredFrequencies
    ) {
        Set<String> indexes = new LinkedHashSet<>();
        for (String frequency : configuredFrequencies) {
            String normalized = normalize(frequency);
            if ("433MHZ".equals(normalized) || "900MHZ".equals(normalized) || "915MHZ".equals(normalized)) {
                indexes.add("1");
            } else if ("1.5GHZ".equals(normalized) || "1.5G".equals(normalized)) {
                indexes.add("2");
            } else if ("2.4GHZ".equals(normalized) || "2.4G".equals(normalized)
                    || "5.8GHZ".equals(normalized) || "5.8G".equals(normalized)) {
                indexes.add("3");
            } else if ("1.2GHZ".equals(normalized) || "1.2G".equals(normalized)) {
                indexes.add("4");
            }
        }
        if (indexes.isEmpty()) {
            indexes.add("-1");
        }

        List<CountermeasureNettyCommand> startCommands = new ArrayList<>();
        List<CountermeasureNettyCommand> stopCommands = new ArrayList<>();
        for (String index : indexes) {
            startCommands.add(new CountermeasureNettyCommand(
                    device.collectFlag(),
                    "device-attack",
                    buildArgs(index, false)
            ));
            stopCommands.add(new CountermeasureNettyCommand(
                    device.collectFlag(),
                    "device-attack",
                    buildArgs(index, true)
            ));
        }
        return new CountermeasureJammingPlan(startCommands, stopCommands, "茂德全向干扰");
    }

    private Map<String, Object> buildArgs(String index, boolean cancel) {
        Map<String, Object> args = new HashMap<>();
        args.put("index", index);
        args.put("iscancel", Boolean.toString(cancel));
        return args;
    }

    private String normalize(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase();
    }
}

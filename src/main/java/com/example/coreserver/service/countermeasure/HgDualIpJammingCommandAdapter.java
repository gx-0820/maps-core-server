package com.example.coreserver.service.countermeasure;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class HgDualIpJammingCommandAdapter implements CountermeasureJammingCommandAdapter {

    @Override
    public String adapterId() {
        return CountermeasureDeviceDirectoryService.ADAPTER_HG_DUAL_IP;
    }

    @Override
    public CountermeasureJammingPlan buildPlan(
            CountermeasureDeviceDirectoryService.CountermeasureRuntimeDevice device,
            List<String> configuredFrequencies
    ) {
        Set<String> frequencies = new LinkedHashSet<>();
        for (String rawFrequency : configuredFrequencies) {
            String normalized = normalize(rawFrequency);
            if ("433MHZ".equals(normalized) || "900MHZ".equals(normalized) || "915MHZ".equals(normalized)) {
                frequencies.add("900MHz");
            } else if ("1.5GHZ".equals(normalized) || "1.5G".equals(normalized)) {
                frequencies.add("1.5GHz");
            } else if ("2.4GHZ".equals(normalized) || "2.4G".equals(normalized)) {
                frequencies.add("2.4GHz");
            } else if ("5.8GHZ".equals(normalized) || "5.8G".equals(normalized)) {
                frequencies.add("5.8GHz");
            }
        }
        if (frequencies.isEmpty()) {
            frequencies.add("900MHz");
            frequencies.add("1.5GHz");
            frequencies.add("2.4GHz");
            frequencies.add("5.8GHz");
        }

        List<CountermeasureNettyCommand> startCommands = new ArrayList<>();
        List<CountermeasureNettyCommand> stopCommands = new ArrayList<>();
        for (String frequency : frequencies) {
            startCommands.add(new CountermeasureNettyCommand(
                    device.collectFlag(),
                    "device-attack",
                    buildArgs(frequency, false)
            ));
            stopCommands.add(new CountermeasureNettyCommand(
                    device.collectFlag(),
                    "device-attack",
                    buildArgs(frequency, true)
            ));
        }
        return new CountermeasureJammingPlan(startCommands, stopCommands, "海格CQ-RF双IP全向干扰");
    }

    private Map<String, Object> buildArgs(String frequency, boolean cancel) {
        Map<String, Object> args = new HashMap<>();
        args.put("frequency", frequency);
        args.put("sec", 10);
        args.put("cancel", cancel);
        return args;
    }

    private String normalize(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase();
    }
}

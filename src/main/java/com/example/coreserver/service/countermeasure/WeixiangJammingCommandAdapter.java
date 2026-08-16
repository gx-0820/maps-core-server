package com.example.coreserver.service.countermeasure;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class WeixiangJammingCommandAdapter implements CountermeasureJammingCommandAdapter {

    private static final String START_COMMAND = "jamming";
    private static final String STOP_COMMAND = "stop_jamming";

    @Override
    public String adapterId() {
        return CountermeasureDeviceDirectoryService.ADAPTER_WEIXIANG_OMNI;
    }

    @Override
    public CountermeasureJammingPlan buildPlan(
            CountermeasureDeviceDirectoryService.CountermeasureRuntimeDevice device,
            List<String> configuredFrequencies
    ) {
        Set<String> normalizedFrequencies = new LinkedHashSet<>();
        for (String configuredFrequency : configuredFrequencies) {
            String normalized = normalize(configuredFrequency);
            if ("433MHZ".equals(normalized) || "900MHZ".equals(normalized) || "915MHZ".equals(normalized)) {
                normalizedFrequencies.add("900MHZ");
            } else if ("L1".equals(normalized) || "GPSL1".equals(normalized) || "GPS_L1".equals(normalized)
                    || "GPS L1".equals(normalized) || "L1:GPS L1".equals(normalized)) {
                normalizedFrequencies.add("GPS_L1");
            } else if ("2.4GHZ".equals(normalized) || "2.4G".equals(normalized)) {
                normalizedFrequencies.add("2.4GHZ");
            } else if ("5.8GHZ".equals(normalized) || "5.8G".equals(normalized)) {
                normalizedFrequencies.add("5.8GHZ");
            }
        }

        List<CountermeasureNettyCommand> startCommands = new ArrayList<>();
        List<CountermeasureNettyCommand> stopCommands = new ArrayList<>();

        if (normalizedFrequencies.contains("900MHZ")) {
            startCommands.add(new CountermeasureNettyCommand(
                    device.collectFlag(),
                    START_COMMAND,
                    buildArgs(Map.of("band_900m", "true"))
            ));
            stopCommands.add(stopCommand(device.collectFlag()));
        }
        if (normalizedFrequencies.contains("GPS_L1")) {
            startCommands.add(new CountermeasureNettyCommand(
                    device.collectFlag(),
                    START_COMMAND,
                    buildArgs(Map.of("band_gps_l1", "true"))
            ));
            stopCommands.add(stopCommand(device.collectFlag()));
        }

        boolean has2g4 = normalizedFrequencies.contains("2.4GHZ");
        boolean has5g8 = normalizedFrequencies.contains("5.8GHZ");
        if (has2g4 || has5g8) {
            Map<String, Object> bandArgs = new HashMap<>();
            if (has2g4) {
                bandArgs.put("band_2g4", "true");
            }
            if (has5g8) {
                bandArgs.put("band_5g8", "true");
            }
            startCommands.add(new CountermeasureNettyCommand(
                    device.collectFlag(),
                    START_COMMAND,
                    buildArgs(bandArgs)
            ));
            stopCommands.add(stopCommand(device.collectFlag()));
        }

        return new CountermeasureJammingPlan(startCommands, stopCommands, "威翔全向干扰");
    }

    private CountermeasureNettyCommand stopCommand(String deviceCode) {
        return new CountermeasureNettyCommand(deviceCode, STOP_COMMAND, null);
    }

    private Map<String, Object> buildArgs(Map<String, Object> bandArgs) {
        Map<String, Object> args = new HashMap<>(bandArgs);
        args.put("sec", 10);
        return args;
    }

    private String normalize(String raw) {
        return raw == null ? "" : raw.trim().replace(" ", "").toUpperCase();
    }
}

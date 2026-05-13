package com.example.coreserver.dto;

import lombok.Data;

@Data
public class DailyDroneStats {
    private int droneCount;
    private int illegalDroneCount;
    private int disposedDroneCount;
}

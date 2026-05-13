package com.example.coreserver.dto;

import lombok.Data;

@Data
public class MonthlyDroneStats {
    private String yearMonth;
    private int droneCount;
    private int illegalDroneCount;
    private int disposedDroneCount;

    public MonthlyDroneStats(String yearMonth, int droneCount, int illegalDroneCount, int disposedDroneCount) {
        this.yearMonth = yearMonth;
        this.droneCount = droneCount;
        this.illegalDroneCount = illegalDroneCount;
        this.disposedDroneCount = disposedDroneCount;
    }
}
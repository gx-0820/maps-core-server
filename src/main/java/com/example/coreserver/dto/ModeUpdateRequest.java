package com.example.coreserver.dto;

import com.example.coreserver.entity.countermeasure.CountermeasureMode;

public class ModeUpdateRequest {
    private CountermeasureMode mode;

    public CountermeasureMode getMode() {
        return mode;
    }

    public void setMode(CountermeasureMode mode) {
        this.mode = mode;
    }
}
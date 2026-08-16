package com.example.coreserver.dto;

import com.example.coreserver.entity.countermeasure.CountermeasureMode;
import com.example.coreserver.entity.countermeasure.CountermeasureOmcFlag;

public class ModeUpdateRequest {
    private CountermeasureMode mode;
    private CountermeasureOmcFlag omcFlag;

    public CountermeasureMode getMode() {
        return mode;
    }

    public void setMode(CountermeasureMode mode) {
        this.mode = mode;
    }

    public CountermeasureOmcFlag getOmcFlag() {
        return omcFlag;
    }

    public void setOmcFlag(CountermeasureOmcFlag omcFlag) {
        this.omcFlag = omcFlag;
    }
}

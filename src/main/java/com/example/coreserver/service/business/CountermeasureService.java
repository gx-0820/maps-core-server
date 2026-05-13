package com.example.coreserver.service.business;

import com.example.coreserver.dto.CountermeasureRequest;
import com.example.coreserver.entity.countermeasure.CountermeasureMode;

public interface CountermeasureService {
//    String handleThreat(CountermeasureRequest request, CountermeasureMode currentMode);

    String handleThreat(CountermeasureRequest request);
}
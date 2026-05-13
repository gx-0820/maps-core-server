package com.example.coreserver.service.device;

import java.util.List;
import java.util.Map;

public interface DeviceDataService {
    List<Map<String, Object>> getRadarDataList();
    List<Map<String, Object>> getRadarDataByDeviceId(String deviceId);
    
    List<Map<String, Object>> getPhotoelectricDataList();
    List<Map<String, Object>> getPhotoelectricDataByDeviceId(String deviceId);
    
    List<Map<String, Object>> getElectricInvestigationDataList();
} 
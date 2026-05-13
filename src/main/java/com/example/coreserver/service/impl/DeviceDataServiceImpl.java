package com.example.coreserver.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.coreserver.entity.data.ElectricInvestigationEntity;
import com.example.coreserver.entity.data.PhotoelectricEntity;
import com.example.coreserver.entity.data.RadarEntity;
import com.example.coreserver.repository.data.ElectricInvestigationEntityRepository;
import com.example.coreserver.repository.data.PhotoelectricEntityRepository;
import com.example.coreserver.repository.data.RadarEntityRepository;
import com.example.coreserver.service.device.DeviceDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DeviceDataServiceImpl implements DeviceDataService {

    @Autowired
    private RadarEntityRepository radarRepository;
    
    @Autowired
    private PhotoelectricEntityRepository photoelectricRepository;
    
    @Autowired
    private ElectricInvestigationEntityRepository electricInvestigationRepository;

    @Override
    public List<Map<String, Object>> getRadarDataList() {
        log.info("查询雷达数据列表，只返回包含deviceId的targets数组");
        List<RadarEntity> entities = radarRepository.findTop100ByOrderByTimestampDesc();
        List<Map<String, Object>> targets = convertRadarEntities(entities);
        log.info("处理后的targets数量: {}", targets.size());
        return targets;
    }

    @Override
    public List<Map<String, Object>> getRadarDataByDeviceId(String deviceId) {
        log.info("查询设备{}的雷达数据，只返回包含deviceId的targets数组", deviceId);
        List<RadarEntity> entities = radarRepository.findByDeviceId(deviceId);
        List<Map<String, Object>> targets = convertRadarEntities(entities);
        log.info("处理后的设备{}的targets数量: {}", deviceId, targets.size());
        return targets;
    }

    private List<Map<String, Object>> convertRadarEntities(List<RadarEntity> entities) {
        List<Map<String, Object>> allTargets = new ArrayList<>();
        
        for (RadarEntity entity : entities) {
            if (entity.getOriginalJson() != null && !entity.getOriginalJson().isEmpty()) {
                try {
                    // 解析原始JSON
                    JSONObject jsonObject = JSON.parseObject(entity.getOriginalJson());
                    String deviceId = entity.getDeviceId();
                    
                    // 获取targets数组
                    if (jsonObject.containsKey("targets")) {
                        JSONArray targetsArray = jsonObject.getJSONArray("targets");
                        if (targetsArray != null) {
                            log.debug("设备{}有{}个targets", deviceId, targetsArray.size());
                            
                            // 处理每个target
                            for (int i = 0; i < targetsArray.size(); i++) {
                                JSONObject target = targetsArray.getJSONObject(i);
                                
                                // 转换为Map以便于增加字段
                                Map<String, Object> targetMap = new HashMap<>(target);
                                
                                // 在target中添加deviceId和timestamp
                                targetMap.put("deviceId", deviceId);
//                                targetMap.put("timestamp", entity.getTimestamp());
                                
                                allTargets.add(targetMap);
                            }
                        }
                    } else {
                        log.debug("设备{}的JSON数据中没有targets字段", deviceId);
                    }
                } catch (Exception e) {
                    log.error("处理雷达目标数据失败，实体ID: {}, 错误: {}", entity.getId(), e.getMessage());
                }
            }
        }
        
        log.info("总共处理了{}个雷达目标", allTargets.size());
        return allTargets;
    }

    @Override
    public List<Map<String, Object>> getPhotoelectricDataList() {
        List<PhotoelectricEntity> entities = photoelectricRepository.findTop100ByOrderByTimestampDesc();
        return convertPhotoelectricEntities(entities);
    }

    @Override
    public List<Map<String, Object>> getPhotoelectricDataByDeviceId(String deviceId) {
        List<PhotoelectricEntity> entities = photoelectricRepository.findByDeviceId(deviceId);
        return convertPhotoelectricEntities(entities);
    }

    private List<Map<String, Object>> convertPhotoelectricEntities(List<PhotoelectricEntity> entities) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (PhotoelectricEntity entity : entities) {
            if (entity.getOriginalJson() != null && !entity.getOriginalJson().isEmpty()) {
                try {
                    JSONObject jsonObject = JSON.parseObject(entity.getOriginalJson());
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", entity.getId());
                    data.put("deviceId", entity.getDeviceId());
                    data.put("timestamp", entity.getTimestamp());
                    data.put("type", entity.getType());
                    data.put("data", jsonObject);
                    result.add(data);
                } catch (Exception e) {
                    log.error("Error parsing JSON for PhotoelectricEntity with id {}: {}", entity.getId(), e.getMessage());
                }
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getElectricInvestigationDataList() {
        List<ElectricInvestigationEntity> entities = electricInvestigationRepository.findTop100ByOrderByTimestampDesc();
        return convertElectricInvestigationEntities(entities);
    }

    private List<Map<String, Object>> convertElectricInvestigationEntities(List<ElectricInvestigationEntity> entities) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ElectricInvestigationEntity entity : entities) {
            if (entity.getOriginalJson() != null && !entity.getOriginalJson().isEmpty()) {
                try {
                    JSONObject jsonObject = JSON.parseObject(entity.getOriginalJson());
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", entity.getId());
                    data.put("timestamp", entity.getTimestamp());
                    data.put("type", entity.getType());
                    data.put("data", jsonObject);
                    result.add(data);
                } catch (Exception e) {
                    log.error("Error parsing JSON for ElectricInvestigationEntity with id {}: {}", entity.getId(), e.getMessage());
                }
            }
        }
        return result;
    }
} 
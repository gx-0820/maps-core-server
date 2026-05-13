package com.example.coreserver.service.impl;

import com.example.coreserver.entity.algorithm.db.DataFusionEntity;
import com.example.coreserver.repository.DataFusionRepository;
import com.example.coreserver.service.algorithm.DataFusionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Primary
public class DataFusionServiceImpl implements DataFusionService {

    @Autowired
    private DataFusionRepository dataFusionRepository;

    @Override
    public List<Map<String, Object>> getDataFusionList() {
        List<DataFusionEntity> entities = dataFusionRepository.findTop100ByOrderByCreatedAtDesc();
        return convertDataFusionEntities(entities);
    }

    @Override
    public List<Map<String, Object>> getDataFusionByTargetId(Integer targetId) {
        List<DataFusionEntity> entities = dataFusionRepository.findByTargetId(targetId);
        return convertDataFusionEntities(entities);
    }

    /**
     * 将数据融合实体列表转换为Map列表
     * @param entities 数据融合实体列表
     * @return Map列表
     */
    private List<Map<String, Object>> convertDataFusionEntities(List<DataFusionEntity> entities) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (DataFusionEntity entity : entities) {
            Map<String, Object> data = new HashMap<>();
            
            // 添加所有字段到Map
            data.put("id", entity.getId());
            data.put("targetId", entity.getTargetId());
            data.put("longitude", entity.getLongitude());
            data.put("latitude", entity.getLatitude());
            data.put("altitude", entity.getAltitude());
            data.put("velocity", entity.getVelocity());
            data.put("azimuth", entity.getAzimuth());
            data.put("type", entity.getType());
            data.put("name", entity.getName());
            data.put("distance", entity.getDistance());
            data.put("pitch", entity.getPitch());
            data.put("threatLevel", entity.getThreatLevel());
            data.put("panAngle", entity.getPanAngle());
            data.put("tiltAngle", entity.getTiltAngle());
            data.put("zoomLevel", entity.getZoomLevel());
            data.put("color", entity.getColor());
            data.put("createdAt", entity.getCreatedAt());
            
            result.add(data);
        }
        log.debug("转换了 {} 条数据融合记录", result.size());
        return result;
    }
} 
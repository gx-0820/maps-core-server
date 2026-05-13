package com.example.coreserver.service.algorithm;

import java.util.List;
import java.util.Map;

public interface DataFusionService {
    /**
     * 获取数据融合列表
     * @return 数据融合列表
     */
    List<Map<String, Object>> getDataFusionList();
    
    /**
     * 根据目标ID获取数据融合信息
     * @param targetId 目标ID
     * @return 数据融合信息
     */
    List<Map<String, Object>> getDataFusionByTargetId(Integer targetId);
} 
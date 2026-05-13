package com.example.coreserver.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreserver.entity.DataRadarTarget;
import com.example.coreserver.entity.DataTdoaTarget;
import com.example.coreserver.mapper.DataRadarTargetMapper;
import com.example.coreserver.service.DataRadarTargetService;
import com.example.coreserver.utils.DataBatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 70411
 * @description 针对表【data_radar_target(雷达目标全量数据表 - 存储雷达设备上报的目标探测时序数据)】的数据库操作Service实现
 * @createDate 2026-04-11 15:37:16
 */
@Slf4j
@Service
public class DataRadarTargetServiceImpl extends ServiceImpl<DataRadarTargetMapper, DataRadarTarget>
        implements DataRadarTargetService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final String BUSINESS_KEY = "DataRadarTarget";

    private final DataBatchUtils<DataRadarTarget> dataBatchUtils;

    public DataRadarTargetServiceImpl(DataBatchUtils<DataRadarTarget> dataBatchUtils) {
        this.dataBatchUtils = dataBatchUtils;
        // 注册批量处理器
        dataBatchUtils.register(
                BUSINESS_KEY,
                this::batchSave,  // 保存回调方法
                1000,             // 最大批量大小
                5000              // 刷新间隔5秒
        );
    }


    @Override
    public void saveBatchData(List<DataRadarTarget> dataRadarTargets) {
        // 解析JSON
//        List<DataRadarTarget> dataRadarTarget = convertToDataRadarTargetList(message);

        if (dataRadarTargets.isEmpty()) {
            return;
        }

        // 添加到批量队列
        dataBatchUtils.addAll(BUSINESS_KEY, dataRadarTargets);
    }


    /**
     * 将雷达JSON数据转换为DataRadarTarget列表
     * @param jsonString 雷达上报的JSON数据
     * @return List<DataRadarTarget> 拆分后的目标列表
     */
    @Override
    public List<DataRadarTarget> convertToDataRadarTargetList(String jsonString) {
        List<DataRadarTarget> targetList = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(jsonString);

            // 1. 解析公共字段（所有target共享的字段）
            String deviceId = root.has("deviceId") ? root.get("deviceId").asText() : null;
            String protocolType = root.has("protocolType") ? root.get("protocolType").asText() : null;
            Integer frameCount = root.has("frameCount") ? root.get("frameCount").asInt() : null;
            Integer searchCycle = root.has("searchCycle") ? root.get("searchCycle").asInt() : null;
            Integer pulseGroupNumber = root.has("pulseGroupNumber") ? root.get("pulseGroupNumber").asInt() : null;
            Integer totalTargetCount = root.has("totalTargetCount") ? root.get("totalTargetCount").asInt() : null;
            Integer validTargetCount = root.has("validTargetCount") ? root.get("validTargetCount").asInt() : null;
            Integer isActive = root.has("isActive") ? (root.get("isActive").asBoolean() ? 1 : 0) : 0;

            // 解析timestamp（微秒精度）
            Date timestamp = null;
            if (root.has("timestamp")) {
                String timestampStr = root.get("timestamp").asText();
                // 处理ISO 8601格式的时间戳
                timestamp = java.sql.Timestamp.valueOf(timestampStr.replace("T", " "));
            }

            // 解析searchDirection
            BigDecimal searchDirection = null;
            if (root.has("searchDirection")) {
                searchDirection = BigDecimal.valueOf(root.get("searchDirection").asDouble());
            }

            // 2. 获取targets数组
            JsonNode targetsNode = root.get("targets");
            if (targetsNode != null && targetsNode.isArray()) {

                for (JsonNode targetNode : targetsNode) {
                    DataRadarTarget entity = new DataRadarTarget();

//                    // 设置UUID主键
//                    entity.setId(UUID.randomUUID().toString());

                    // 设置公共字段
                    entity.setTimestamp(timestamp);
                    entity.setDeviceId(deviceId);
                    entity.setProtocolType(protocolType);
                    entity.setFrameCount(frameCount);
                    entity.setSearchDirection(searchDirection);
                    entity.setSearchCycle(searchCycle);
                    entity.setPulseGroupNumber(pulseGroupNumber);
                    entity.setTotalTargetCount(totalTargetCount);
                    entity.setValidTargetCount(validTargetCount);
                    entity.setIsActive(isActive);

                    // 设置target特有字段
                    if (targetNode.has("targetId")) {
                        entity.setTargetId(targetNode.get("targetId").asInt());
                    }

                    if (targetNode.has("snr")) {
                        entity.setSnr(BigDecimal.valueOf(targetNode.get("snr").asDouble()));
                    }

                    if (targetNode.has("range")) {
                        entity.setRange(BigDecimal.valueOf(targetNode.get("range").asDouble()));
                    }

                    if (targetNode.has("azimuth2")) {
                        entity.setAzimuth2(BigDecimal.valueOf(targetNode.get("azimuth2").asDouble()));
                    }

                    if (targetNode.has("pitch")) {
                        entity.setPitch(BigDecimal.valueOf(targetNode.get("pitch").asDouble()));
                    }

                    if (targetNode.has("speed")) {
                        entity.setSpeed(BigDecimal.valueOf(targetNode.get("speed").asDouble()));
                    }

                    if (targetNode.has("altitude")) {
                        entity.setAltitude(BigDecimal.valueOf(targetNode.get("altitude").asDouble()));
                    }

                    if (targetNode.has("targetLat")) {
                        entity.setTargetLat(BigDecimal.valueOf(targetNode.get("targetLat").asDouble()));
                    }

                    if (targetNode.has("targetLon")) {
                        entity.setTargetLon(BigDecimal.valueOf(targetNode.get("targetLon").asDouble()));
                    }

                    if (targetNode.has("targetType")) {
                        entity.setTargetType(targetNode.get("targetType").asInt());
                    }

                    if (targetNode.has("selectionFlag")) {
                        entity.setSelectionFlag(targetNode.get("selectionFlag").asInt());
                    }

                    if (targetNode.has("xSpeed")) {
                        entity.setXSpeed(BigDecimal.valueOf(targetNode.get("xSpeed").asDouble()));
                    }

                    if (targetNode.has("ySpeed")) {
                        entity.setYSpeed(BigDecimal.valueOf(targetNode.get("ySpeed").asDouble()));
                    }

                    if (targetNode.has("zSpeed")) {
                        entity.setZSpeed(BigDecimal.valueOf(targetNode.get("zSpeed").asDouble()));
                    }

                    // 设置默认值
                    entity.setIsDelete(0); // 0-正常

                    // targetBatch字段如果JSON中没有，可以设置默认值或从其他字段派生
                     entity.setTargetBatch(targetNode.has("targetBatch") ? targetNode.get("targetBatch").asLong() : -1L);

                    targetList.add(entity);
                }
            } else {
                log.warn("JSON中不存在targets数组或数组为空");
            }

        } catch (Exception e) {
            log.error("解析雷达JSON数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("雷达数据转换失败", e);
        }

        return targetList;
    }


    /**
     * 批量保存回调方法
     *
     * @param dataList
     */
    private void batchSave(List<DataRadarTarget> dataList) {
        try {
            // 使用MyBatis-Plus的saveBatch方法
            boolean result = saveBatch(dataList);
            if (result) {
                log.info("批量保存成功，数量: {}", dataList.size());
            } else {
                log.error("批量保存失败");
                throw new RuntimeException("批量保存失败");
            }
        } catch (Exception e) {
            log.error("批量保存异常", e);
            throw new RuntimeException("批量保存异常", e);
        }
    }

}





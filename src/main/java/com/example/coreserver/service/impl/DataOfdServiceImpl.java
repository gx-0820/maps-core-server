package com.example.coreserver.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreserver.entity.DataOfd;
import com.example.coreserver.entity.DataTdoaTarget;
import com.example.coreserver.mapper.DataOfdMapper;
import com.example.coreserver.service.DataOfdService;
import com.example.coreserver.utils.DataBatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author 70411
 * @description 针对表【data_ofd(光电设备数据表)】的数据库操作Service实现
 * @createDate 2026-04-15 16:23:05
 */
@Slf4j
@Service
public class DataOfdServiceImpl extends ServiceImpl<DataOfdMapper, DataOfd>
        implements DataOfdService {


    public static final String BUSINESS_KEY = "DataOfd";

    private final DataBatchUtils<DataOfd> dataBatchUtils;

    public DataOfdServiceImpl(DataBatchUtils<DataOfd> dataBatchUtils) {
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
    public void saveBatchData(DataOfd dataOfd) {
//        DataOfd dataOfd = convertToDataOfd(jsonNode);
        if (dataOfd == null) {
            return;
        }

        // 添加到批量队列
        dataBatchUtils.addAll(BUSINESS_KEY, List.of(dataOfd));
    }

    @Override
    public  DataOfd convertToDataOfd(JsonNode deviceStatusNode) {
        if (deviceStatusNode == null) {
            log.warn("deviceStatusNode is null");
            return null;
        }

        DataOfd dataOfd = new DataOfd();

        try {
            // 基本信息
            if (deviceStatusNode.has("deviceId") && !deviceStatusNode.get("deviceId").isNull()) {
                dataOfd.setDeviceId(deviceStatusNode.get("deviceId").asText());
            }

            if (deviceStatusNode.has("timestamp") && !deviceStatusNode.get("timestamp").isNull()) {
                String timestampStr = deviceStatusNode.get("timestamp").asText();
                // 根据实际时间格式解析，这里假设是标准格式
                try {
                    dataOfd.setTimestamp(new Date(Long.parseLong(timestampStr)));
                } catch (NumberFormatException e) {
                    // 如果不是时间戳，尝试其他格式
                    dataOfd.setTimestamp(new Date());
                }
            }

            dataOfd.setCreateTime(new Date());
            dataOfd.setUpdateTime(new Date());

            // 解析 measurement 节点
            JsonNode measurementNode = deviceStatusNode.get("measurement");
            if (measurementNode != null) {
                if (measurementNode.has("laserDistance") && !measurementNode.get("laserDistance").isNull()) {
                    dataOfd.setLaserDistance(measurementNode.get("laserDistance").asDouble());
                }
                if (measurementNode.has("azimuthAngle") && !measurementNode.get("azimuthAngle").isNull()) {
                    dataOfd.setAzimuthAngle(measurementNode.get("azimuthAngle").asDouble());
                }
                if (measurementNode.has("pitchAngle") && !measurementNode.get("pitchAngle").isNull()) {
                    dataOfd.setPitchAngle(measurementNode.get("pitchAngle").asDouble());
                }
                if (measurementNode.has("azimuthSpeed") && !measurementNode.get("azimuthSpeed").isNull()) {
                    dataOfd.setAzimuthSpeed(measurementNode.get("azimuthSpeed").asDouble());
                }
                if (measurementNode.has("pitchSpeed") && !measurementNode.get("pitchSpeed").isNull()) {
                    dataOfd.setPitchSpeed(measurementNode.get("pitchSpeed").asDouble());
                }
                if (measurementNode.has("azimuthError") && !measurementNode.get("azimuthError").isNull()) {
                    dataOfd.setAzimuthError(measurementNode.get("azimuthError").asDouble());
                }
                if (measurementNode.has("pitchError") && !measurementNode.get("pitchError").isNull()) {
                    dataOfd.setPitchError(measurementNode.get("pitchError").asDouble());
                }
                if (measurementNode.has("laserEnergy") && !measurementNode.get("laserEnergy").isNull()) {
                    dataOfd.setLaserEnergy(measurementNode.get("laserEnergy").asDouble());
                }
            }

            // 解析 status 节点
            JsonNode statusNode = deviceStatusNode.get("status");
            if (statusNode != null) {
                if (statusNode.has("isAutoMode") && !statusNode.get("isAutoMode").isNull()) {
                    dataOfd.setAutoMode(statusNode.get("isAutoMode").asText());
                }
                if (statusNode.has("trackingStatus") && !statusNode.get("trackingStatus").isNull()) {
                    dataOfd.setTrackingStatus(statusNode.get("trackingStatus").asText());
                }
                if (statusNode.has("trackingChannel") && !statusNode.get("trackingChannel").isNull()) {
                    dataOfd.setTrackingChannel(statusNode.get("trackingChannel").asText());
                }
                if (statusNode.has("servoPowerStatus") && !statusNode.get("servoPowerStatus").isNull()) {
                    dataOfd.setServoPowerStatus(statusNode.get("servoPowerStatus").asText());
                }
                if (statusNode.has("servoReadyStatus") && !statusNode.get("servoReadyStatus").isNull()) {
                    dataOfd.setServoReadyStatus(statusNode.get("servoReadyStatus").asText());
                }
                if (statusNode.has("isCorrelation") && !statusNode.get("isCorrelation").isNull()) {
                    dataOfd.setIsCorrelation(statusNode.get("isCorrelation").asText());
                }
                if (statusNode.has("isPolarityBlack") && !statusNode.get("isPolarityBlack").isNull()) {
                    dataOfd.setIsPolarityBlack(statusNode.get("isPolarityBlack").asText());
                }
                if (statusNode.has("serverPowerStatus") && !statusNode.get("serverPowerStatus").isNull()) {
                    dataOfd.setServerPowerStatus(statusNode.get("serverPowerStatus").asText());
                }
            }

        } catch (Exception e) {
            log.error("Error converting JsonNode to DataOfd: {}", e.getMessage());
            return null;
        }

        return dataOfd;
    }


    /**
     * 批量保存回调方法
     *
     * @param dataList
     */
    private void batchSave(List<DataOfd> dataList) {
        try {
            // 使用MyBatis-Plus的saveBatch方法
            boolean result = this.saveBatch(dataList);
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





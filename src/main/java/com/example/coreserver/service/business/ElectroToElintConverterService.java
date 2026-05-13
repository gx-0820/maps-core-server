package com.example.coreserver.service.business;

import com.example.coreserver.entity.DataElintTarget;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 电侦目标数据转换工具类
 * 用于将电侦原始数据转换为电侦目标对象
 */
@Slf4j
@Component
public class ElectroToElintConverterService {

    private final ObjectMapper objectMapper;

    public ElectroToElintConverterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 将电侦原始报文转换为电侦目标对象列表
     * 
     * @param electroRawData 电侦原始报文JSON字符串
     * @return 电侦目标对象列表
     */
    public List<DataElintTarget> convertElectroToElint(String electroRawData) {
        List<DataElintTarget> elintTargets = new ArrayList<>();
        
        if (electroRawData == null || electroRawData.trim().isEmpty()) {
            log.warn("电侦原始报文为空");
            return elintTargets;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(electroRawData);
            
            // 检查是否为数组节点
            if (rootNode.isArray()) {
                for (JsonNode targetNode : rootNode) {
                    DataElintTarget elintTarget = parseSingleElectroTarget(targetNode);
                    if (elintTarget != null) {
                        elintTargets.add(elintTarget);
                    }
                }
            } else {
                // 单个目标的情况
                DataElintTarget elintTarget = parseSingleElectroTarget(rootNode);
                if (elintTarget != null) {
                    elintTargets.add(elintTarget);
                }
            }
        } catch (Exception e) {
            log.error("解析电侦原始报文失败: {}", e.getMessage(), e);
        }
        
        return elintTargets;
    }

    /**
     * 将单个电侦目标JSON字符串转换为电侦目标对象
     * 
     * @param singleElectroTarget 单个电侦目标JSON字符串
     * @return 电侦目标对象，解析失败返回null
     */
    public DataElintTarget convertSingleElectroToElint(String singleElectroTarget) {
        if (singleElectroTarget == null || singleElectroTarget.trim().isEmpty()) {
            log.warn("单个电侦目标数据为空");
            return null;
        }

        try {
            JsonNode targetNode = objectMapper.readTree(singleElectroTarget);
            return parseSingleElectroTarget(targetNode);
        } catch (Exception e) {
            log.error("解析单个电侦目标失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析单个电侦目标节点为电侦目标对象
     * 
     * @param targetNode 电侦目标JSON节点
     * @return 电侦目标对象
     */
    private DataElintTarget parseSingleElectroTarget(JsonNode targetNode) {
        try {
            DataElintTarget elintTarget = new DataElintTarget();
            
            // 设置时间戳 - 使用detectTime或updateTime
            String detectTimeStr = targetNode.has("detectTime") ? 
                targetNode.get("detectTime").asText() : 
                targetNode.has("updateTime") ? 
                targetNode.get("updateTime").asText() : null;
                
            if (detectTimeStr != null && !detectTimeStr.isEmpty()) {
                try {
                    // 处理ISO 8601格式的时间字符串
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
                    Date timestamp = sdf.parse(detectTimeStr);
                    elintTarget.setTimestamp(timestamp);
                } catch (Exception e) {
                    log.warn("解析时间字符串失败: {}, 使用当前时间", detectTimeStr);
                    elintTarget.setTimestamp(new Date());
                }
            } else {
                elintTarget.setTimestamp(new Date());
            }
            
            // 设置目标批次号 - 使用detectCounter作为批次号
            if (targetNode.has("detectCounter")) {
                elintTarget.setDetectCounter(targetNode.get("detectCounter").asInt());
            }
            
            // 设置原始ID
            if (targetNode.has("rawID")) {
                elintTarget.setRawID(targetNode.get("rawID").asText());
            }
            
            // 设置真实业务ID
            if (targetNode.has("realID")) {
                elintTarget.setRealID(targetNode.get("realID").asText());
            }
            
            // 设置目标类型 drone/rc
            if (targetNode.has("type")) {
                elintTarget.setType(targetNode.get("type").asText());
            }
            
            // 设置探测源标识
            if (targetNode.has("finder")) {
                elintTarget.setFinder(targetNode.get("finder").asText());
            }
            
            // 设置是否远程ID
            if (targetNode.has("isRemoteID")) {
                elintTarget.setIsRemoteID(targetNode.get("isRemoteID").asBoolean());
            }
            
            // 设置无人机型号
            if (targetNode.has("model")) {
                elintTarget.setModel(targetNode.get("model").asText());
            }
            
            // 设置信号频率
            if (targetNode.has("freq")) {
                elintTarget.setFreq(targetNode.get("freq").asText());
            }
            
            // 设置威胁等级
            if (targetNode.has("threat")) {
                elintTarget.setThreat(targetNode.get("threat").asInt());
            }
            
            // 设置图标地址
            if (targetNode.has("iconUrl")) {
                elintTarget.setIconUrl(targetNode.get("iconUrl").asText());
            }
            
            // 设置持续发现次数
            if (targetNode.has("seenTimes")) {
                elintTarget.setSeenTimes(targetNode.get("seenTimes").asInt());
            }
            
            // 设置坐标信息
            if (targetNode.has("lon")) {
                double lon = targetNode.get("lon").asDouble();
                if (lon != 0.0) { // 排除默认值0.0
                    elintTarget.setLon(BigDecimal.valueOf(lon));
                }
            }
            
            if (targetNode.has("lat")) {
                double lat = targetNode.get("lat").asDouble();
                if (lat != 0.0) { // 排除默认值0.0
                    elintTarget.setLat(BigDecimal.valueOf(lat));
                }
            }
            
            if (targetNode.has("alt")) {
                String altStr = targetNode.get("alt").asText();
                if (altStr != null && !altStr.equals("0.0")) {
                    elintTarget.setAlt(altStr);
                }
            }
            
            // 设置遥控器位置信息（如果有）
            if (targetNode.has("rcLon")) {
                double rcLon = targetNode.get("rcLon").asDouble();
                if (rcLon != 0.0) {
                    elintTarget.setRcLon(BigDecimal.valueOf(rcLon));
                }
            }
            
            if (targetNode.has("rcLat")) {
                double rcLat = targetNode.get("rcLat").asDouble();
                if (rcLat != 0.0) {
                    elintTarget.setRcLat(BigDecimal.valueOf(rcLat));
                }
            }
            
            // 设置智能打击相关字段
            if (targetNode.has("canSmartAttack")) {
                elintTarget.setCanSmartAttack(targetNode.get("canSmartAttack").asBoolean());
            }
            
            if (targetNode.has("isSmartAttack")) {
                elintTarget.setIsSmartAttack(targetNode.get("isSmartAttack").asBoolean());
            }
            
            // 设置白名单相关字段
            if (targetNode.has("whiteListable")) {
                elintTarget.setWhiteListable(targetNode.get("whiteListable").asBoolean());
            }
            
            if (targetNode.has("whiteListName")) {
                elintTarget.setWhiteListName(targetNode.get("whiteListName").asText());
            }
            
            // 设置探测时间
            if (targetNode.has("detectTime")) {
                String detectTime = targetNode.get("detectTime").asText();
                if (detectTime != null && !detectTime.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
                        Date time = sdf.parse(detectTime);
                        elintTarget.setDetectTime(time);
                    } catch (Exception e) {
                        log.warn("解析探测时间失败: {}", detectTime);
                    }
                }
            }
            
            // 设置更新时间
            if (targetNode.has("updateTime")) {
                String updateTime = targetNode.get("updateTime").asText();
                if (updateTime != null && !updateTime.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
                        Date time = sdf.parse(updateTime);
                        elintTarget.setUpdateTime(time);
                    } catch (Exception e) {
                        log.warn("解析更新时间失败: {}", updateTime);
                    }
                }
            }
            
            // 设置通信协议
            if (targetNode.has("protocol")) {
                elintTarget.setProtocol(targetNode.get("protocol").asText());
            }
            
            // 设置忽略相关字段
            if (targetNode.has("canIgnore")) {
                elintTarget.setCanIgnore(targetNode.get("canIgnore").asBoolean());
            }
            
            if (targetNode.has("isIgnored")) {
                elintTarget.setIsIgnored(targetNode.get("isIgnored").asBoolean());
            }
            
            // 设置云台联动相关字段
            if (targetNode.has("canPTZTo")) {
                elintTarget.setCanPTZTo(targetNode.get("canPTZTo").asBoolean());
            }
            
            // 处理details相关信息
            if (targetNode.has("details_targetID")) {
                elintTarget.setDetailsTargetID(targetNode.get("details_targetID").asText());
            }
            
            if (targetNode.has("details_deviceID")) {
                elintTarget.setDetailsDeviceID(targetNode.get("details_deviceID").asText());
            }
            
            if (targetNode.has("details_deviceName")) {
                elintTarget.setDetailsDeviceName(targetNode.get("details_deviceName").asText());
            }
            
            if (targetNode.has("details_deviceLon")) {
                double deviceLon = targetNode.get("details_deviceLon").asDouble();
                if (deviceLon != 0.0) {
                    elintTarget.setDetailsDeviceLon(BigDecimal.valueOf(deviceLon));
                }
            }
            
            if (targetNode.has("details_deviceLat")) {
                double deviceLat = targetNode.get("details_deviceLat").asDouble();
                if (deviceLat != 0.0) {
                    elintTarget.setDetailsDeviceLat(BigDecimal.valueOf(deviceLat));
                }
            }
            
            if (targetNode.has("details_finder")) {
                elintTarget.setDetailsFinder(targetNode.get("details_finder").asText());
            }
            
            if (targetNode.has("details_detectCounter")) {
                elintTarget.setDetailsDetectCounter(targetNode.get("details_detectCounter").asInt());
            }
            
            if (targetNode.has("details_azimuth")) {
                elintTarget.setDetailsAzimuth(targetNode.get("details_azimuth").asText());
            }
            
            if (targetNode.has("details_distance")) {
                String distanceStr = targetNode.get("details_distance").asText();
                if (distanceStr != null && !distanceStr.equals("0.0")) {
                    try {
                        elintTarget.setDetailsDistance(new BigDecimal(distanceStr));
                    } catch (NumberFormatException e) {
                        log.warn("解析距离值失败: {}", distanceStr);
                    }
                }
            }
            
            if (targetNode.has("details_updateTime")) {
                String detailsUpdateTime = targetNode.get("details_updateTime").asText();
                if (detailsUpdateTime != null && !detailsUpdateTime.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
                        Date time = sdf.parse(detailsUpdateTime);
                        elintTarget.setDetailsUpdateTime(time);
                    } catch (Exception e) {
                        log.warn("解析详情更新时间失败: {}", detailsUpdateTime);
                    }
                }
            }
            
            return elintTarget;
            
        } catch (Exception e) {
            log.error("解析单个电侦目标失败: {}", e.getMessage(), e);
            return null;
        }
    }

}
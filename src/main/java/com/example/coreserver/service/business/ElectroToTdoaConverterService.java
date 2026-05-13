package com.example.coreserver.service.business;

import com.example.coreserver.entity.DataTdoaTarget;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 电侦目标数据转TDOA工具类
 * 用于将电侦原始数据转换为TDOA目标对象
 */
@Slf4j
@Component
public class ElectroToTdoaConverterService {

    private final ObjectMapper objectMapper;

    public ElectroToTdoaConverterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 将电侦原始报文转换为TDOA目标对象列表
     * 
     * @param electroRawData 电侦原始报文JSON字符串
     * @return TDOA目标对象列表
     */
    public List<DataTdoaTarget> convertElectroToTdoa(String electroRawData) {
        List<DataTdoaTarget> tdoaTargets = new ArrayList<>();
        
        if (electroRawData == null || electroRawData.trim().isEmpty()) {
            log.warn("电侦原始报文为空");
            return tdoaTargets;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(electroRawData);
            
            // 检查是否为数组节点
            if (rootNode.isArray()) {
                for (JsonNode targetNode : rootNode) {
                    DataTdoaTarget tdoaTarget = parseSingleElectroTarget(targetNode);
                    if (tdoaTarget != null) {
                        tdoaTargets.add(tdoaTarget);
                    }
                }
            } else {
                // 单个目标的情况
                DataTdoaTarget tdoaTarget = parseSingleElectroTarget(rootNode);
                if (tdoaTarget != null) {
                    tdoaTargets.add(tdoaTarget);
                }
            }
        } catch (Exception e) {
            log.error("解析电侦原始报文失败: {}", e.getMessage(), e);
        }
        
        return tdoaTargets;
    }

    /**
     * 将电侦原始报文转换为指定格式的JSON数组
     * 
     * @param electroRawData 电侦原始报文JSON字符串
     * @return 符合指定格式的JSON数组字符串
     */
    public ArrayNode convertElectroToJsonArray(String electroRawData) {
        if (electroRawData == null || electroRawData.trim().isEmpty()) {
            log.warn("电侦原始报文为空");
            return objectMapper.createArrayNode();
        }

        try {
            JsonNode rootNode = objectMapper.readTree(electroRawData);
            ArrayNode resultArray = objectMapper.createArrayNode();
            
            // 检查是否为数组节点
            if (rootNode.isArray()) {
                for (JsonNode targetNode : rootNode) {
                    ObjectNode formattedNode = formatSingleElectroTargetToJson(targetNode);
                    if (formattedNode != null) {
                        resultArray.add(formattedNode);
                    }
                }
            } else {
                // 单个目标的情况
                ObjectNode formattedNode = formatSingleElectroTargetToJson(rootNode);
                if (formattedNode != null) {
                    resultArray.add(formattedNode);
                }
            }
            
            return resultArray;
        } catch (Exception e) {
            log.error("转换电侦数据为JSON数组失败: {}", e.getMessage(), e);
            return objectMapper.createArrayNode();
        }
    }

    /**
     * 将单个电侦目标JSON字符串转换为TDOA目标对象
     * 
     * @param singleElectroTarget 单个电侦目标JSON字符串
     * @return TDOA目标对象，解析失败返回null
     */
    public DataTdoaTarget convertSingleElectroToTdoa(String singleElectroTarget) {
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
     * 格式化单个电侦目标节点为指定格式的JSON对象
     * 
     * @param targetNode 电侦目标JSON节点
     * @return 格式化后的JSON对象
     */
    private ObjectNode formatSingleElectroTargetToJson(JsonNode targetNode) {
        try {
            ObjectNode resultNode = objectMapper.createObjectNode();
            
            // 设置外层时间戳（毫秒级）
            long currentTimeMillis = System.currentTimeMillis();
            resultNode.put("timestamp", currentTimeMillis);
            
            // 创建rawData对象
            ObjectNode rawDataNode = objectMapper.createObjectNode();
            rawDataNode.put("type", "TDOA");
            
            // 设置设备ID - 从details中获取deviceID
            String deviceId = "TDOA01"; // 默认值
            String sensorId = "";
            BigDecimal sensorLongitude = BigDecimal.ZERO;
            BigDecimal sensorLatitude = BigDecimal.ZERO;
            
            if (targetNode.has("details") && targetNode.get("details").isArray()) {
                JsonNode detailsArray = targetNode.get("details");
                if (detailsArray.size() > 0) {
                    JsonNode firstDetail = detailsArray.get(0);
                    if (firstDetail.has("deviceID")) {
                        deviceId = firstDetail.get("deviceID").asText();
                    }
                    if (firstDetail.has("deviceName")) {
                        sensorId = firstDetail.get("deviceName").asText();
                    }
                    // 设置传感器位置信息
                    if (firstDetail.has("deviceLon")) {
                        sensorLongitude = BigDecimal.valueOf(firstDetail.get("deviceLon").asDouble());
                    }
                    if (firstDetail.has("deviceLat")) {
                        sensorLatitude = BigDecimal.valueOf(firstDetail.get("deviceLat").asDouble());
                    }
                }
            }
            rawDataNode.put("device_id", deviceId);
            
            // 设置目标批次号 - 使用detectCounter作为批次号
            long targetBatch = currentTimeMillis;
            if (targetNode.has("detectCounter")) {
                targetBatch = targetNode.get("detectCounter").asLong();
            }
            rawDataNode.put("target_batch", targetBatch);
            
            // 设置无人机ID - 使用rawID或id
            String uavId = targetNode.has("rawID") ? 
                targetNode.get("rawID").asText() : 
                targetNode.has("id") ? 
                targetNode.get("id").asText() : "";
            rawDataNode.put("uav_id", uavId);

            // 设置无人机型号
            String uavModel = targetNode.has("model") ? targetNode.get("model").asText() : "";
            rawDataNode.put("uav_model", uavModel);
            
            // 设置无人机型号编号（如果没有具体信息，设为0）
            rawDataNode.put("uav_model_no", 0);
            
            // 用户ID（默认为空）
            rawDataNode.put("user_id", "");
            
            // 轨迹ID（根据业务规则生成，这里使用uavId的一部分或其他标识）
            //String traceId = uavId.length() > 8 ? uavId.substring(uavId.length() - 8) : uavId;
            rawDataNode.put("trace_id", uavId);
            
            // 设置坐标信息
            double uavLon = targetNode.has("lon") ? targetNode.get("lon").asDouble() : 0.0;
            double uavLat = targetNode.has("lat") ? targetNode.get("lat").asDouble() : 0.0;
            double uavAlt = 0.0;
            if (targetNode.has("alt")) {
                try {
                    uavAlt = Double.parseDouble(targetNode.get("alt").asText());
                } catch (NumberFormatException e) {
                    log.warn("解析高度值失败: {}", targetNode.get("alt").asText());
                }
            }
            
            rawDataNode.put("uav_lon", uavLon);
            rawDataNode.put("uav_lat", uavLat);
            rawDataNode.put("uav_alt", uavAlt);
            rawDataNode.put("uav_height", uavAlt); // 假设相对高度与海拔相同
            
            // 速度和偏航角（如果电侦数据中有这些信息则使用，否则设为默认值）
            rawDataNode.put("velocity", 0.0);
            rawDataNode.put("yaw", 0.0);
            
            // 设置遥控器位置信息
            double pilotLon = targetNode.has("rcLon") ? targetNode.get("rcLon").asDouble() : 0.0;
            double pilotLat = targetNode.has("rcLat") ? targetNode.get("rcLat").asDouble() : 0.0;
            rawDataNode.put("pilot_lon", pilotLon);
            rawDataNode.put("pilot_lat", pilotLat);
            
            // 返航点坐标（如果电侦数据中没有，则使用遥控器位置或默认值）
            rawDataNode.put("home_lon", pilotLon);
            rawDataNode.put("home_lat", pilotLat);
            
            // 时间相关字段
            rawDataNode.put("timestamp", currentTimeMillis);
            rawDataNode.put("start_from", currentTimeMillis - 3000); // 假设3秒前开始
            rawDataNode.put("duration", 3); // 假设持续3秒
            
            // 频率信息
            long frequency = 0L;
            if (targetNode.has("freq")) {
                try {
                    String freqStr = targetNode.get("freq").asText();
                    if (freqStr.contains(".")) {
                        // 移除小数点并转换为数字，然后乘以1000000转换为Hz
                        String freqWithoutDot = freqStr.replace(".", "");
                        frequency = Long.parseLong(freqWithoutDot) * 1000000L;
                    } else {
                        frequency = Long.parseLong(freqStr) * 1000000L;
                    }
                } catch (NumberFormatException e) {
                    log.warn("解析频率值失败: {}", targetNode.get("freq").asText());
                }
            }
            rawDataNode.put("frequency", frequency);
            
            // 区域标记和白名单ID
            rawDataNode.put("area_flag", 0);
            rawDataNode.put("white_list_id", 0);
            
            // 目标类型
            int targetType = 13; // 默认值
            if (targetNode.has("threat")) {
                int threat = targetNode.get("threat").asInt();
                targetType = threat > 50 ? 1 : 13; // 根据威胁等级调整
            }
            if (targetNode.has("type") && "drone".equals(targetNode.get("type").asText())) {
                targetType = 13; // 无人机类型
            }
            rawDataNode.put("target_type", targetType);
            
            // 传感器相关信息
            rawDataNode.put("sensor_topic", "zgbdsd"); // 默认主题
            rawDataNode.put("sensor_id", sensorId);
            rawDataNode.put("sensor_longitude", sensorLongitude.doubleValue());
            rawDataNode.put("sensor_latitude", sensorLatitude.doubleValue());
            rawDataNode.put("sensor_altitude", 0.0);
            
            // 方位角和距离（如果需要计算，可以根据坐标计算，这里设为默认值）
            rawDataNode.put("uav_azimuth", 0.0);
            rawDataNode.put("uav_distance", 0.0);
            
            // 设备UUID（可以使用某种唯一标识生成）
            String deviceUuid = java.util.UUID.randomUUID().toString().replace("-", "");
            rawDataNode.put("device_uuid", deviceUuid);
            
            // 扩展设备ID
            rawDataNode.putNull("extension_device_id");
            
            // 将rawData添加到结果节点
            resultNode.set("rawData", rawDataNode);
            
            // 地理围栏（空数组）
            ArrayNode geofenceArray = objectMapper.createArrayNode();
            resultNode.set("geofence", geofenceArray);
            
            return resultNode;
            
        } catch (Exception e) {
            log.error("格式化单个电侦目标为JSON失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析单个电侦目标节点为TDOA目标对象
     * 
     * @param targetNode 电侦目标JSON节点
     * @return TDOA目标对象
     */
    private DataTdoaTarget parseSingleElectroTarget(JsonNode targetNode) {
        try {
            DataTdoaTarget tdoaTarget = new DataTdoaTarget();
            
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
                    tdoaTarget.setTimestamp(timestamp);
                } catch (Exception e) {
                    log.warn("解析时间字符串失败: {}, 使用当前时间", detectTimeStr);
                    tdoaTarget.setTimestamp(new Date());
                }
            } else {
                tdoaTarget.setTimestamp(new Date());
            }
            
            // 设置设备ID - 从details中获取deviceID
            if (targetNode.has("details") && targetNode.get("details").isArray()) {
                JsonNode detailsArray = targetNode.get("details");
                if (detailsArray.size() > 0) {
                    JsonNode firstDetail = detailsArray.get(0);
                    if (firstDetail.has("deviceID")) {
                        tdoaTarget.setDeviceId(firstDetail.get("deviceID").asText());
                    }
                    if (firstDetail.has("deviceName")) {
                        tdoaTarget.setSensorId(firstDetail.get("deviceName").asText());
                    }
                    // 设置传感器位置信息
                    if (firstDetail.has("deviceLon")) {
                        tdoaTarget.setSensorLongitude(BigDecimal.valueOf(firstDetail.get("deviceLon").asDouble()));
                    }
                    if (firstDetail.has("deviceLat")) {
                        tdoaTarget.setSensorLatitude(BigDecimal.valueOf(firstDetail.get("deviceLat").asDouble()));
                    }
                }
            }
            
            // 设置目标批次号 - 使用detectCounter作为批次号
            if (targetNode.has("detectCounter")) {
                tdoaTarget.setTargetBatch((long) targetNode.get("detectCounter").asInt());
            }
            
            // 设置无人机ID - 使用rawID或id
            String uavId = targetNode.has("rawID") ? 
                targetNode.get("rawID").asText() : 
                targetNode.has("id") ? 
                targetNode.get("id").asText() : null;
            tdoaTarget.setUavId(uavId);
            tdoaTarget.setTraceId(uavId);

            // 设置无人机型号
            if (targetNode.has("model")) {
                tdoaTarget.setUavModel(targetNode.get("model").asText());
            }
            
            // 设置威胁等级
            if (targetNode.has("threat")) {
                // 根据threat值设置目标类型或其他相关字段
                int threat = targetNode.get("threat").asInt();
                // 这里可以根据具体业务逻辑映射threat到targetType
                tdoaTarget.setTargetType(threat > 50 ? 1 : 0); // 示例：威胁大于50设为高威胁类型
            }
            
            // 设置坐标信息
            if (targetNode.has("lon")) {
                double lon = targetNode.get("lon").asDouble();
                if (lon != 0.0) { // 排除默认值0.0
                    tdoaTarget.setUavLon(BigDecimal.valueOf(lon));
                }
            }
            
            if (targetNode.has("lat")) {
                double lat = targetNode.get("lat").asDouble();
                if (lat != 0.0) { // 排除默认值0.0
                    tdoaTarget.setUavLat(BigDecimal.valueOf(lat));
                }
            }
            
            if (targetNode.has("alt")) {
                String altStr = targetNode.get("alt").asText();
                if (altStr != null && !altStr.equals("0.0")) {
                    try {
                        tdoaTarget.setUavAlt(new BigDecimal(altStr));
                    } catch (NumberFormatException e) {
                        log.warn("解析高度值失败: {}", altStr);
                    }
                }
            }
            
            // 设置遥控器位置信息（如果有）
            if (targetNode.has("rcLon")) {
                double rcLon = targetNode.get("rcLon").asDouble();
                if (rcLon != 0.0) {
                    tdoaTarget.setPilotLon(BigDecimal.valueOf(rcLon));
                }
            }
            
            if (targetNode.has("rcLat")) {
                double rcLat = targetNode.get("rcLat").asDouble();
                if (rcLat != 0.0) {
                    tdoaTarget.setPilotLat(BigDecimal.valueOf(rcLat));
                }
            }
            
            // 设置协议类型 - DataTdoaTarget中没有此字段，跳过
            // if (targetNode.has("protocol")) {
            //     tdoaTarget.setProtocolType(targetNode.get("protocol").asText());
            // }
            
            // 设置其他相关信息
            if (targetNode.has("type")) {
                // 可以设置目标类型相关的字段
                String type = targetNode.get("type").asText();
                if ("drone".equals(type)) {
                    tdoaTarget.setTargetType(1); // 假设1代表无人机类型
                }
            }
            
            // 设置频率信息
            if (targetNode.has("freq")) {
                try {
                    // 将字符串频率转换为长整型，例如 "5796.5" -> 57965
                    String freqStr = targetNode.get("freq").asText();
                    if (freqStr.contains(".")) {
                        // 移除小数点并转换为数字
                        String freqWithoutDot = freqStr.replace(".", "");
                        tdoaTarget.setFrequency(Long.parseLong(freqWithoutDot));
                    } else {
                        tdoaTarget.setFrequency(Long.parseLong(freqStr));
                    }
                } catch (NumberFormatException e) {
                    log.warn("解析频率值失败: {}", targetNode.get("freq").asText());
                }
            }

            return tdoaTarget;
            
        } catch (Exception e) {
            log.error("解析单个电侦目标失败: {}", e.getMessage(), e);
            return null;
        }
    }

}
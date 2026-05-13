package com.example.coreserver.wsserver.services.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.example.coreserver.entity.DataElintTarget;
import com.example.coreserver.entity.DataTdoaTarget;
import com.example.coreserver.service.DataElintTargetService;
import com.example.coreserver.service.DataTdoaTargetService;
import com.example.coreserver.service.algorithm.AlgorithmGrpcClient;
import com.example.coreserver.service.business.ElectroToElintConverterService;
import com.example.coreserver.service.business.ElectroToTdoaConverterService;
import com.example.coreserver.service.threat.ThreatAssessmentService;
import com.example.coreserver.wsserver.base.AbsDeviceProcess;
import com.example.coreserver.wsserver.base.WSType;
import com.example.coreserver.wsserver.netty.NettyDataHolder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ElintPromosProcessImpl extends AbsDeviceProcess {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ElectroToTdoaConverterService electroToTdoaConverterService;
    @Autowired
    private DataTdoaTargetService dataTdoaTargetService;
    @Autowired
    private DataElintTargetService dataElintTargetService;
    @Autowired
    private AlgorithmGrpcClient algorithmGrpcClient;
    @Autowired
    private ElectroToElintConverterService electroToElintConverterService;
    @Autowired
    private ThreatAssessmentService threatAssessmentService;

    // 创建一个调度执行器服务
    static private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    protected ElintPromosProcessImpl(NettyDataHolder nettyDataHolder) {
        super(nettyDataHolder);
        this.commandIssuingScheduling();
    }

    @Override
    public WSType code() {
        return WSType.ELINT_PROMOS_UAV;
    }

    // 指令下发调度
    public void commandIssuingScheduling() {
        // 1分钟后，60秒查询一次设备信息
        scheduler.scheduleAtFixedRate(() -> {
            this.sendCommandToDeice(this.code().name(), "device-query", null);
        }, 60000, 60000, TimeUnit.MILLISECONDS);
        // 1分钟后，1.1秒查询一次目标信息
        scheduler.scheduleAtFixedRate(() -> {
            this.sendCommandToDeice(this.code().name(), "target-query", null);
        }, 60000, 1100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void message(String message) {
        try {
            // 解析JSON获取data_type
            JSONObject jsonObject = new JSONObject(message);
            String dataType = jsonObject.getStr("data_type");
            
            if (dataType == null || dataType.isEmpty()) {
                log.warn("接收到的消息缺少data_type字段: {}", message);
                return;
            }
            
            // 根据data_type分别处理
            switch (dataType) {
                case "device-attack":
                    handleDeviceAttack(message);
                    break;
                case "device-query":
                    handleDeviceQuery(message);
                    break;
                case "target-query":
                    handleTargetQuery(message);
                    break;
                default:
                    log.warn("未知的data_type: {}", dataType);
                    break;
            }
        } catch (Exception e) {
            log.error("处理电侦消息失败: {}", message, e);
        }
    }

    /**
     * 处理干扰指令响应
     * @param message 原始消息
     */
    private void handleDeviceAttack(String message) {
        try {
            log.info("收到指令下发响应报文: {}", message);
            // 记录日志等
        } catch (Exception e) {
            log.error("处理设备攻击响应失败", e);
        }
    }

    /**
     * 处理设备查询响应
     * @param message 原始消息
     */
    private void handleDeviceQuery(String message) {
        try {
            log.info("收到获取设备信息响应报文: {}", message);
            //处理设备信息
            String deviceData = new JSONObject(message).getStr("data");
            String status = "unknown";
            if(deviceData != null && !deviceData.isEmpty()){
                //推送设备信息到算法
                ObjectNode deviceInfoNode = (ObjectNode) mapper.readTree(deviceData);
                JsonNode statusInfoNode = deviceInfoNode.get("statusInfo");
                if (statusInfoNode != null) {
                    JsonNode mgmtStatusNode = statusInfoNode.get("mgmtStatus");
                    if (mgmtStatusNode != null) {
                        status = mgmtStatusNode.asText();
                        log.info("获取到设备管理状态: {}", status);
                    }
                }
            }
            //推送设备状态信息到WEB端
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("status", status);
            this.sendToWeb("device_status_ELINT_MD", jsonObject);
        } catch (Exception e) {
            log.error("处理设备查询响应失败", e);
        }
    }

    /**
     * 处理目标查询响应
     * @param message 原始消息
     */
    private void handleTargetQuery(String message) {
        try {
            log.info("收到获取侦测目标响应报文: {}", message);
            JSONObject jsonObject = new JSONObject(message);
            String targetData = jsonObject.getStr("data");
            // 推送电侦目标数据到算法
            ArrayNode targetArray = electroToTdoaConverterService.convertElectroToJsonArray(targetData);
            for (JsonNode jsonNode : targetArray) {
                JsonNode uav_lon = jsonNode.get("uav_lon");
                JsonNode uav_lat = jsonNode.get("uav_lat");
                JsonNode uav_alt = jsonNode.get("uav_alt");
                JsonNode uav_height = jsonNode.get("uav_height");
//                // 经纬高如果是null则丢弃
//                if(uav_lon == null || uav_lat == null || uav_alt == null || uav_height == null){
//                    log.info("推送算法经纬高为null, 丢弃数据: {}", jsonNode);
//                    continue;
//                }
                // 推送给融合的数据接口
                log.info("侦测目标转TDOA后推送算法报文: {}", jsonNode);
                algorithmGrpcClient.PushFusionData(jsonNode.toString());
                // 推送给轨迹预测的数据接口
                //algorithmGrpcClient.PushTrackData(mergedData.toString());
            }
            // 电侦数据评估威胁等级
            List<DataTdoaTarget> dataTdoaTargets = electroToTdoaConverterService.convertElectroToTdoa(targetData);
            if (CollectionUtils.isNotEmpty(dataTdoaTargets)){
                for (DataTdoaTarget dataTdoaTarget : dataTdoaTargets) {
                    //dataTdoaTargetService.saveBatchData(dataTdoaTarget);
                    // 危险等级计算
//                    // 经纬高如果是null则丢弃
//                    if(dataTdoaTarget.getUavLon()  == null || dataTdoaTarget.getUavLat() == null || dataTdoaTarget.getUavAlt() == null){
//                        log.info("计算威胁等级经纬高为null, 丢弃数据: {}", dataTdoaTarget);
//                        continue;
//                    }
                    // 对可能为空的字段设置默认值
                    if (dataTdoaTarget.getVelocity() == null) {
                        dataTdoaTarget.setVelocity(BigDecimal.valueOf(0));
                    }
//                    if (dataTdoaTarget.getUavLon() == null) {
//                        dataTdoaTarget.setUavLon(BigDecimal.valueOf(0));
//                    }
//                    if (dataTdoaTarget.getUavLat() == null) {
//                        dataTdoaTarget.setUavLat(BigDecimal.valueOf(0));
//                    }
//                    if (dataTdoaTarget.getUavAlt() == null) {
//                        dataTdoaTarget.setUavAlt(BigDecimal.valueOf(0));
//                    }
                    if (dataTdoaTarget.getUavHeight() == null) {
                        dataTdoaTarget.setUavHeight(BigDecimal.valueOf(0));
                    }
                    threatAssessmentService.tdoaDataHandel(dataTdoaTarget);
                }
                //log.info("侦测目标转TDOA后写入TDOA表，写入 {} 条记录", dataTdoaTargets.size());
            }
            // 保存电侦原始目标数据 写入电侦表
            List<DataElintTarget> dataElintTargets = electroToElintConverterService.convertElectroToElint(targetData);
            if (CollectionUtils.isNotEmpty(dataElintTargets)){
                for (DataElintTarget dataElintTarget : dataElintTargets) {
                    dataElintTargetService.saveBatchData(dataElintTarget);
                }
                log.info("侦测目标写入电侦表，写入 {} 条记录", dataElintTargets.size());
            }
        } catch (Exception e) {
            log.error("处理目标查询响应失败", e);
        }
    }

}

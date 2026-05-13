package com.example.coreserver.service.device;

import com.example.coreserver.entity.Geofence;
import com.example.coreserver.grpc.common.DeviceId;
import com.example.coreserver.grpc.common.Empty;
import com.example.coreserver.grpc.common.Response;
import com.example.coreserver.grpc.uav.*;
import com.example.coreserver.service.business.AzimuthElevationCalculator;
import com.example.coreserver.service.business.GeofenceService;
import com.example.coreserver.vo.DroneVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UavService {

    @GrpcClient("device-server")
    private UavServiceGrpc.UavServiceBlockingStub stub;

    private final GeofenceService geofenceService;

    private AzimuthElevationCalculator AECalculator;

    public UavService(GeofenceService geofenceService, AzimuthElevationCalculator AECalculator) {
        this.geofenceService = geofenceService;
        this.AECalculator = AECalculator;
    }

    public DeviceListResponse getDeviceList() {
        return stub.getElectricInvestigationDevices(Empty.newBuilder().build());
    }

    public SystemConfigResponse getSystemConfig(DeviceId deviceId) {
        return stub.getSystemConfig(deviceId);
    }

    public Response setDefenseConfig(DefenseConfigRequest request) {
        return stub.setDefenseConfig(request);
    }

    public Response setAttackAuto(AttackAutoRequest request) {
        return stub.setAttackAuto(request);
    }

    public Response getTargets(DeviceId deviceId) {
        return stub.getUav(deviceId);
    }

    public List<DroneVO> getUavTargetsAsDroneVO(DeviceId deviceId) {
        Response response = getTargets(deviceId);
        List<DroneVO> droneVOList = new ArrayList<>();

        if (response.getSuccess()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.getMessage());
                // 检查是否为数组节点
                if (rootNode.isArray()) {
                    for (JsonNode target : rootNode) {
                        if (target.get("isRemoteID").asBoolean()){
                            double azimuth = 0;
                            double elevation = 0;
                            double range = 0;
                            double[] res = AECalculator.calculateAzEl(target.get("lat").asDouble(), target.get("lon").asDouble(), target.get("alt").asDouble());
                            azimuth = res[0];
                            elevation = res[1];
                            range = AECalculator.distance3D(target.get("lat").asDouble(), target.get("lon").asDouble(), target.get("alt").asDouble());
                            DroneVO droneVO = DroneVO.builder()
                                    .id(target.get("details").get(0).get("detectCounter").asInt())
                                    .longitude(target.get("lon").asDouble())
                                    .latitude(target.get("lat").asDouble())
                                    .altitude(target.get("alt").asDouble())
                                    .velocity(Double.NaN)
                                    .azimuth(azimuth)
                                    .pitch(elevation)
                                    .type("电侦目标")
                                    .name("电侦目标")
                                    .color("FFFF00")
                                    .threatLevel(calculateThreat(range))
                                    .distance(range)
                                    .createTime(LocalDateTime.parse(LocalDateTime.now().toString()))
//                                    .createTime(LocalDateTime.parse(target.get("createTime").asText()))
                                    .lastUpdateTime(LocalDateTime.now().toString())
                                    .build();
                            droneVOList.add(droneVO);
                        }
                    }
                } else {
                    if (rootNode.get("isRemoteID").asBoolean()){
                        double azimuth = 0;
                        double elevation = 0;
                        double range = 0;
                        double[] res = AECalculator.calculateAzEl(rootNode.get("lat").asDouble(), rootNode.get("lon").asDouble(), rootNode.get("alt").asDouble());
                        azimuth = res[0];
                        elevation = res[1];
                        range = AECalculator.distance3D(rootNode.get("lat").asDouble(), rootNode.get("lon").asDouble(), rootNode.get("alt").asDouble());
                        // 如果不是数组，则尝试解析为单个目标
                        DroneVO droneVO = DroneVO.builder()
                                .id(rootNode.get("details").get(0).get("detectCounter").asInt())
                                .longitude(rootNode.get("lon").asDouble())
                                .latitude(rootNode.get("lat").asDouble())
                                .altitude(rootNode.get("alt").asDouble())
                                .velocity(Double.NaN)
                                .azimuth(azimuth)
                                .pitch(elevation)
                                .type("电侦目标")
                                .name("电侦目标")
                                .color("FFFF00")
                                .threatLevel(calculateThreat(range))
                                .distance(range)
                                .createTime(LocalDateTime.parse(LocalDateTime.now().toString()))
                                .lastUpdateTime(LocalDateTime.now().toString())
                                .build();
                        droneVOList.add(droneVO);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse ELECTRIC_INVESTIGATION targets: {}", e.getMessage());
            }
        }
        return droneVOList;
    }

    private int calculateThreat(double range) {
//        // 从数据库获取所有 Geofence 配置
        List<Geofence> geofences = geofenceService.findAll();

        if (geofences == null || geofences.isEmpty()) {
            log.warn("No Geofence configurations found in the database");
            return 0; // 返回默认威胁级别
        }

        // 假设只使用第一个 Geofence 配置，如果有多个配置，可以添加逻辑选择相应的配置
        Geofence entity = geofences.getFirst();

        Double coreRadius = entity.getCoreRadius();
        Double bufferRadius = entity.getBufferRadius();
        Double alertRadius = entity.getAlertRadius();

        // 检查字段是否为 null
        if (coreRadius == null || bufferRadius == null || alertRadius == null) {
            log.warn("Geofence fields are null");
            return 0; // 返回默认威胁级别
        }

        if (range <= coreRadius) {
            return 3;// 修改标记
        } else if (range <= bufferRadius) {
            return 2;
        } else if (range <= alertRadius) {
            return 1;
        } else {
            return 0;
        }
    }
}

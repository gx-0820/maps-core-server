package com.example.coreserver.service.device;

import com.example.coreserver.grpc.common.DeviceId;
import com.example.coreserver.grpc.common.Empty;
import com.example.coreserver.grpc.common.Response;
import com.example.coreserver.grpc.radar.*;
import com.example.coreserver.grpc.photoelectric.InitNorthRequest;
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
import java.util.Collections;
import java.util.List;
import com.example.coreserver.entity.Geofence;

@Slf4j
@Service
public class RadarService {

    @GrpcClient("device-server")
    private RadarServiceGrpc.RadarServiceBlockingStub stub;

    private final GeofenceService geofenceService;

    private final AzimuthElevationCalculator AECalculator;

    public RadarService(GeofenceService geofenceService, AzimuthElevationCalculator AECalculator) {
        this.geofenceService = geofenceService;
        this.AECalculator = AECalculator;
    }

    public DeviceListResponse getRadarDevices() {
        return stub.getRadarDevices(Empty.newBuilder().build());
    }

    public Response setStandbyMode(DeviceId deviceId) {
        return stub.setStandbyMode(deviceId);
    }

    public Response setSearchMode(SearchModeRequest request) {
        return stub.setSearchMode(request);
    }

    public Response enterTrackMode(TrackModeRequest request) {
        return stub.enterTrackMode(request);
    }

    public Response setTrackerTarget(int targetId) {
        TrackerTargetRequest request = TrackerTargetRequest.newBuilder().setTargetId(targetId).build();
        return stub.setTrackerTarget(request);
    }

    public Response exitTrackMode(ExitTrackRequest request) {
        return stub.exitTrackMode(request);
    }

    public Response getTargets(DeviceId deviceId) {
        return stub.getTargets(deviceId);
    }

    public List<DroneVO> getRadarTargetsAsDroneVO(DeviceId deviceId) {
        Response response = getTargets(deviceId);
        List<DroneVO> droneVOList = new ArrayList<>();

        if (response.getSuccess()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.getMessage());
                // 检查是否为数组节点
                if (rootNode.isArray()) {
//                    long stime = System.currentTimeMillis();
                    for (JsonNode target : rootNode) {
//                        double azimuth = 0;
//                        double elevation = 0;
//                        double range = 0;
//                        double[] res = AECalculator.calculateAzEl(target.get("targetLat").asDouble(), target.get("targetLon").asDouble(), target.get("altitude").asDouble());
//                        azimuth = res[0];
//                        elevation = res[1];
//                        range = AECalculator.distance3D(target.get("targetLat").asDouble(), target.get("targetLon").asDouble(), target.get("altitude").asDouble());
//                        System.out.println("azimuth:"+azimuth+"   range:"+range);
                        DroneVO droneVO = DroneVO.builder()
                                .id(target.get("targetId").asInt())
                                .longitude(target.get("targetLon").asDouble())
                                .latitude(target.get("targetLat").asDouble())
                                .altitude(target.get("altitude").asDouble())
                                .velocity(target.get("speed").asDouble())
                                .azimuth(target.get("azimuth2").asDouble())
                                .pitch(target.get("pitch").asDouble())
//                                .azimuth(target.get())
//                                .pitch(target.get())
                                .type("雷达目标")
                                .name("雷达目标")
                                .color("FFFF00")
                                .threatLevel(calculateThreat(target.get("range").asDouble()))
                                .distance(target.get("range").asDouble())
                                .createTime(LocalDateTime.parse(target.get("createTime").asText()))
                                .lastUpdateTime(LocalDateTime.now().toString())
                                .build();
                        droneVOList.add(droneVO);
                    }
//                    long etime = System.currentTimeMillis();
//                    System.out.printf("执行时长：%d 毫秒./n", (etime - stime));
                } else {
//                    double azimuth = 0;
//                    double elevation = 0;
//                    double range = 0;
//                    double[] res = AECalculator.calculateAzEl(rootNode.get("targetLat").asDouble(), rootNode.get("targetLon").asDouble(), rootNode.get("altitude").asDouble());
//                    azimuth = res[0];
//                    elevation = res[1];
//                    range = AECalculator.distance3D(rootNode.get("targetLat").asDouble(), rootNode.get("targetLon").asDouble(), rootNode.get("altitude").asDouble());
                    // 如果不是数组，则尝试解析为单个目标
                    DroneVO droneVO = DroneVO.builder()
                            .id(rootNode.get("targetId").asInt())
                            .longitude(rootNode.get("targetLon").asDouble())
                            .latitude(rootNode.get("targetLat").asDouble())
                            .altitude(rootNode.get("altitude").asDouble())
                            .velocity(rootNode.get("speed").asDouble())
                            .azimuth(rootNode.get("azimuth2").asDouble())
                            .pitch(rootNode.get("pitch").asDouble())
//                            .azimuth(azimuth)
//                            .pitch(elevation)
                            .type("雷达目标")
                            .name("雷达目标")
                            .color("FFFF00")
                            .threatLevel(calculateThreat(rootNode.get("range").asDouble()))
                            .distance(rootNode.get("range").asDouble())
//                            .threatLevel(calculateThreat(range))
//                            .distance(range)
                            .createTime(LocalDateTime.parse(rootNode.get("createTime").asText()))
                            .lastUpdateTime(LocalDateTime.now().toString())
                            .build();
                    droneVOList.add(droneVO);
                }
            } catch (Exception e) {
                log.error("Failed to parse radar targets: {}", e.getMessage());
            }
        }

        return droneVOList;
    }

    public int calculateThreat(double range) {
        // 从数据库获取所有 Geofence 配置
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

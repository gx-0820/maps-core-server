package com.example.coreserver.controller;

import com.example.coreserver.dto.ResponseDTO;
import com.example.coreserver.grpc.common.DeviceId;
import com.example.coreserver.service.algorithm.AlgorithmDataProcessor;
import com.example.coreserver.service.device.RadarService;
import com.example.coreserver.service.device.UavService;
import com.example.coreserver.vo.DroneVO;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Api(tags = "飞行物融合列表接口")
@RequestMapping("/api/drones")
public class DroneController {

////    带融合算法的获取无人家列表
    private final AlgorithmDataProcessor dataProcessor;
//    // 无人机数据缓存（线程安全）
//    private static final Map<Integer, DroneCacheWrapper> droneCache = new ConcurrentHashMap<>();
//    // 数据有效期（秒）
    private static final int EXPIRE_SECONDS = 3;
//
//    public DroneController(AlgorithmDataProcessor dataProcessor) {
//        this.dataProcessor = dataProcessor;
//        // 启动定时清理任务
//        new Timer().schedule(new CacheCleanTask(), 0, 1000);
//    }
//
//    @GetMapping("/list")
//    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
//    public ResponseDTO<List<DroneVO>> getDroneList() {
//        // 合并实时数据和缓存数据
//        List<DroneVO> realTimeList = dataProcessor.getDroneVOList();
//        updateCache(realTimeList);
//
//        List<DroneVO> result = droneCache.values().stream()
//                .map(DroneCacheWrapper::getDroneVO)
//                .collect(Collectors.toList());
//
//        return ResponseDTO.ok(result);
//    }
//
//    // 更新缓存（新增或刷新存在时间）
//    private void updateCache(List<DroneVO> newList) {
//        LocalDateTime now = LocalDateTime.now();
//        Set<Integer> activeIds = new HashSet<>();
//
//        // 更新现有数据并标记活跃ID
//        for (DroneVO drone : newList) {
//            activeIds.add(drone.getId());
//            droneCache.compute(drone.getId(), (k, v) ->
//                    (v == null) ?
//                            new DroneCacheWrapper(drone, now) :
//                            v.updateDrone(drone, now)
//            );
//        }
//
//        // 立即移除消失的无人机[1,3](@ref)
//        droneCache.keySet().removeIf(id -> !activeIds.contains(id));
//    }
//
//    // 缓存包装类
//    private static class DroneCacheWrapper {
//        @Getter
//        private DroneVO droneVO;
//        private LocalDateTime lastActiveTime;
//
//        public DroneCacheWrapper(DroneVO droneVO, LocalDateTime lastActiveTime) {
//            this.droneVO = droneVO;
//            this.lastActiveTime = lastActiveTime;
//        }
//
//        public DroneCacheWrapper updateDrone(DroneVO newDrone, LocalDateTime updateTime) {
//            this.droneVO = newDrone;
//            this.lastActiveTime = updateTime;
//            return this;
//        }
//
//        public boolean isExpired(LocalDateTime currentTime) {
//            return lastActiveTime.plusSeconds(EXPIRE_SECONDS).isBefore(currentTime);
//        }
//    }
//
//    // 缓存清理任务
//    private static class CacheCleanTask extends TimerTask {
//        @Override
//        public void run() {
//            LocalDateTime now = LocalDateTime.now();
//            droneCache.entrySet().removeIf(entry ->
//                    entry.getValue().isExpired(now)
//            );
//        }
//    }

    // 只使用雷达数据
    private final RadarService radarService;
    private final UavService uavService;

    public DroneController(RadarService radarService, UavService uavService,AlgorithmDataProcessor dataProcessor) {
        this.radarService = radarService;
        this.uavService = uavService;
        this.dataProcessor = dataProcessor;
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseDTO<List<DroneVO>> getDroneList() {
        // 从RadarService获取实时目标数据
        DeviceId deviceId = DeviceId.newBuilder().setDeviceId("RADAR01").build(); // 使用实际的设备ID
        List<DroneVO> radarTargets = radarService.getRadarTargetsAsDroneVO(deviceId);
        return ResponseDTO.ok(radarTargets);
    }
//    将雷达和电侦数据同时推送到前端

    @GetMapping("/radaranduavlist")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseDTO<List<DroneVO>> getRadarAndUavDroneList() {
        // 从RadarService获取实时目标数据
        DeviceId deviceId = DeviceId.newBuilder().setDeviceId("RADAR01").build(); // 使用实际的设备ID
        List<DroneVO> radarTargets = radarService.getRadarTargetsAsDroneVO(deviceId);
        DeviceId deviceId_uav = DeviceId.newBuilder().setDeviceId("SF1311013073").build(); // 使用实际的设备ID
        List<DroneVO> uavTargets = uavService.getUavTargetsAsDroneVO(deviceId_uav);
        List<DroneVO> mergeTargets = new ArrayList<>();
        mergeTargets.addAll(radarTargets);
        mergeTargets.addAll(uavTargets);
        return ResponseDTO.ok(mergeTargets);
    }

//    @Scheduled(fixedRate = 10000)
//    public void test(){
//        List<DroneVO> realTimeList = dataProcessor.getDroneVOList();
//
//        System.out.printf("realTimeList=%s",realTimeList);
//    }



}
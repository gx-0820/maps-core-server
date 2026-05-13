package com.example.coreserver.controller;

import com.example.coreserver.grpc.common.DeviceId;
import com.example.coreserver.grpc.common.Response;
import com.example.coreserver.grpc.radar.*;
import com.example.coreserver.service.device.RadarService;
import com.google.protobuf.util.JsonFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 雷达控制接口
 * @author: zhanghenan
 */
@Slf4j
@RestController
@RequestMapping("/api/radar")
@Tag(name = "RadarController", description = "雷达设备控制接口")
public class RadarController {

    @Autowired
    private RadarService radarService;

    @Operation(summary = "获取雷达设备列表")
    @GetMapping("/devices")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getRadarDevices() {
        try {
            DeviceListResponse radarDevices = radarService.getRadarDevices();
            log.info("Found {} radar devices", radarDevices.getDevicesCount());
            return ResponseEntity.ok(JsonFormat.printer().print(radarDevices));
        } catch (Exception e) {
            log.error("Failed to get radar devices {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to get radar devices");
        }
    }

    @Operation(summary = "设置待机模式")
    @PostMapping("/standby")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setStandbyMode(@RequestBody String deviceId) {
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(deviceId, builder);
            Response response = radarService.setStandbyMode(builder.build());
            log.info("Set standby mode, deviceId: {}", deviceId);
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set StandbyMode {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set StandbyMode");        }
    }

    @Operation(summary = "设置搜索模式")
    @PostMapping("/search")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setSearchMode(@RequestBody String request) {
        try {
            SearchModeRequest.Builder builder = SearchModeRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            SearchModeRequest modeRequest = builder.build();

            // 参数验证
            if (modeRequest.getRadarCount() < 1 || modeRequest.getRadarCount() > 4) {
                return ResponseEntity.internalServerError().body("雷达数量必须在1-4之间");
            }

            Response response = radarService.setSearchMode(modeRequest);

            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set SearchMode {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set SearchMode");
        }
    }

    @Operation(summary = "进入跟踪模式")
    @PostMapping("/track/enter")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> enterTrackMode(@RequestBody String request) {
        try {
            TrackModeRequest.Builder builder = TrackModeRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = radarService.enterTrackMode(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to enter track mode {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to enter track mode");
        }
    }

    @Operation(summary = "退出跟踪模式")
    @PostMapping("/track/exit")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> exitTrackMode(@RequestBody String request) {
        try {
            ExitTrackRequest.Builder builder = ExitTrackRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = radarService.exitTrackMode(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to exit track mode {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to exit track mode");
        }
    }

//    @Operation(summary = "获取飞行物列表")
    @PostMapping("/targets")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getTargets(@RequestBody String deviceId) {
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(deviceId, builder);
            DeviceId id = builder.build();
            Response targets = radarService.getTargets(id);
            return ResponseEntity.ok(JsonFormat.printer().print(targets));
        } catch (Exception e) {
            log.error("Failed to get targets {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to get targets");
        }
    }

}


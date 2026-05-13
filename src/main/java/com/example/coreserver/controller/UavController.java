package com.example.coreserver.controller;

import com.example.coreserver.grpc.common.DeviceId;
import com.example.coreserver.grpc.common.Response;
import com.example.coreserver.grpc.uav.AttackAutoRequest;
import com.example.coreserver.grpc.uav.DefenseConfigRequest;
import com.example.coreserver.grpc.uav.DeviceListResponse;
import com.example.coreserver.grpc.uav.SystemConfigResponse;
import com.example.coreserver.service.device.UavService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;


/**
 * 无人机控制接口
 * @author: zhanghenan
 */
@Slf4j
@RestController
@RequestMapping("/api/uav")
@Tag(name = "UavController", description = "无人机设备控制接口")
public class UavController {

    @Autowired
    private UavService uavService;

    private static final DateTimeFormatter DF = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Operation(summary = "无线-查询设备信息")
    @GetMapping("/getDeviceList")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getDeviceList() {
        try {
            DeviceListResponse deviceList = uavService.getDeviceList();
            log.info("Found {} uav devices", deviceList.getDevicesCount());
            return ResponseEntity.ok(JsonFormat.printer().print(deviceList));
        } catch (Exception e) {
            log.error("Failed to get uav devices {}",e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

//    @Operation(summary = "无线-查询系统配置")
    @PostMapping("/getSystemConfig")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getSystemConfig(@RequestBody String deviceId) {
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(deviceId, builder);
            SystemConfigResponse systemConfig = uavService.getSystemConfig(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(systemConfig));
        } catch (Exception e) {
            log.error("Failed to get system config {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Operation(summary = "无线-设置防御配置")
    @PostMapping("/setDefenseConfig")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setDefenseConfig(@RequestBody String request) {
        try {
            DefenseConfigRequest.Builder builder = DefenseConfigRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = uavService.setDefenseConfig(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set defense config {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Operation(summary = "无线电侦操作")
    @PostMapping("/setAttackAuto")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setAttackAuto(@RequestBody String request) {
        try {
            AttackAutoRequest.Builder builder = AttackAutoRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = uavService.setAttackAuto(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set attack auto {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

//    @Operation(summary = "获取无人机列表")
    @PostMapping("/targets")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getTargets(@RequestBody String deviceId) {
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(deviceId, builder);
            Response response = uavService.getTargets(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to get uav {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

//    // 模拟电侦列表接口
//    @PostMapping(value = "/targets", produces = "application/json")
//    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
//    public String getTargets(@RequestBody String body) {
//        String deviceId;
//        try {
//            JsonNode node = MAPPER.readTree(body);
//            deviceId = node.path("device_id").asText("electric001");
//        } catch (Exception e) {
//            deviceId = "electric001";
//        }
//
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < 1; i++) {
//            String now = DF.format(OffsetDateTime.now());
//            sb.append("{\n")
//                    .append("  \"type\": \"ELECTRIC_UAV\",\n")
//                    .append("  \"UAVtype\": \"drone\",\n")
//                    .append("  \"deviceId\": \"").append(deviceId).append("\",\n")
//                    .append("  \"timestamp\": \"").append(now).append("\",\n")
//                    .append("  \"model\": \"Mini4 Pro\",\n")
//                    .append("  \"freq\": \"5.8GHz\",\n")
//                    .append("  \"threat\": \"100\",\n")
//                    .append("  \"detectTime\": \"").append(now).append("\",\n")
//                    .append("  \"seenTimes\": 3\n")
//                    .append("}\n");
//        }
//        return sb.toString();
//    }
}



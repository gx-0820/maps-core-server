package com.example.coreserver.controller;

import com.example.coreserver.service.device.DeviceDataService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备数据接口
 * @author gaoxin
 */
@Slf4j
@RestController
@RequestMapping("/api/device-data")
@Tag(name = "DeviceDataController", description = "设备数据接口")
public class DeviceDataController {

    @Autowired
    private DeviceDataService deviceDataService;

//    @Operation(summary = "获取雷达数据列表")
    @GetMapping("/radar")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<List<Map<String, Object>>> getRadarDataList() {
        try {
            List<Map<String, Object>> targetsList = deviceDataService.getRadarDataList();
            // log.info("返回 {} 条雷达目标记录", targetsList.size());
            return ResponseEntity.ok(targetsList);
        } catch (Exception e) {
            log.error("获取雷达目标数据失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

//    @Operation(summary = "按设备ID获取雷达数据")
    @GetMapping("/radar/{deviceId}")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<List<Map<String, Object>>> getRadarDataByDeviceId(@PathVariable String deviceId) {
        try {
            List<Map<String, Object>> targetsList = deviceDataService.getRadarDataByDeviceId(deviceId);
            log.info("返回设备 {} 的 {} 条雷达目标记录", deviceId, targetsList.size());
            return ResponseEntity.ok(targetsList);
        } catch (Exception e) {
            log.error("获取设备 {} 的雷达目标数据失败: {}", deviceId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

//    @Operation(summary = "获取光电数据列表")
    @GetMapping("/photoelectric")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<List<Map<String, Object>>> getPhotoelectricDataList() {
        try {
            List<Map<String, Object>> dataList = deviceDataService.getPhotoelectricDataList();
            log.info("Retrieved {} photoelectric data records", dataList.size());
            return ResponseEntity.ok(dataList);
        } catch (Exception e) {
            log.error("Failed to retrieve photoelectric data: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

//    @Operation(summary = "按设备ID获取光电数据")
    @GetMapping("/photoelectric/{deviceId}")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<List<Map<String, Object>>> getPhotoelectricDataByDeviceId(@PathVariable String deviceId) {
        try {
            List<Map<String, Object>> dataList = deviceDataService.getPhotoelectricDataByDeviceId(deviceId);
            log.info("Retrieved {} photoelectric data records for device {}", dataList.size(), deviceId);
            return ResponseEntity.ok(dataList);
        } catch (Exception e) {
            log.error("Failed to retrieve photoelectric data for device {}: {}", deviceId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

//    @Operation(summary = "获取电侦数据列表")
    @GetMapping("/electric-investigation")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<List<Map<String, Object>>> getElectricInvestigationDataList() {
        try {
            List<Map<String, Object>> dataList = deviceDataService.getElectricInvestigationDataList();
            log.info("Retrieved {} electric investigation data records", dataList.size());
            return ResponseEntity.ok(dataList);
        } catch (Exception e) {
            log.error("Failed to retrieve electric investigation data: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 
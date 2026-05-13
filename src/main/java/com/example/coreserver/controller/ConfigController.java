package com.example.coreserver.controller;

import cn.hutool.http.useragent.UserAgent;
import com.alibaba.fastjson2.JSON;
import com.example.coreserver.entity.Config;
import com.example.coreserver.entity.User;
import com.example.coreserver.grpc.common.DeviceId;
import com.example.coreserver.grpc.common.Response;
import com.example.coreserver.grpc.config.Device;
import com.example.coreserver.grpc.config.DeviceConfig;
import com.example.coreserver.service.device.ConfigService;
import com.example.coreserver.utils.BaseContext;
import com.example.coreserver.utils.ConfigUtils;
import com.example.coreserver.utils.JwtUtil;
import com.google.protobuf.util.JsonFormat;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 配置控制接口
 *
 * @author: zhanghenan
 */
@RestController
@RequestMapping("/api/config")
@Slf4j
@Tag(name = "ConfigController", description = "设备配置接口")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @PostMapping("/getConfig")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public Object getConfig() {
        try {
            DeviceConfig config = configService.getConfig();
            log.info("Read {} configuration successfully", config.getDevicesCount());
            return ResponseEntity.ok(JsonFormat.printer().print(config));
        } catch (Exception e) {
            log.error("Failed to read configuration {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Failed to read configuration: " + e.getMessage());
        }
    }

//    @PutMapping
//    public ResponseEntity<?> updateConfig(@RequestBody DeviceConfig config) {
//        try {
//            configService.updateConfig(config);
//            return ResponseEntity.ok("Configuration updated successfully");
//        } catch (ConfigurationException e) {
//            log.error("Configuration update failed: {}", e.getMessage());
//            return ResponseEntity.badRequest().body(e.getMessage());
//        } catch (Exception e) {
//            log.error("Unexpected error while updating configuration: {}", e.getMessage(), e);
//            return ResponseEntity.internalServerError()
//                    .body("Unexpected error: " + e.getMessage());
//        }
//    }

    @PostMapping("/addDevices")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> addDevices(@RequestBody String Devices) {
        try {

            DeviceConfig.Builder builder = DeviceConfig.newBuilder();
            JsonFormat.parser().merge(Devices, builder);

            Response response = configService.addDevices(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to add devices {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/devices/{deviceId}")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> removeDevice(@PathVariable String deviceId) {
        try {
            Response response = configService.removeDevice(DeviceId.newBuilder().setDeviceId(deviceId).build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Unexpected error while removing device {}: {}", deviceId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @PostMapping("/addDevice")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> addDevice(@RequestBody String device) {
        try {
            System.out.println(device);
            Device.Builder builder = Device.newBuilder();
            JsonFormat.parser().ignoringUnknownFields().merge(device, builder);

            Response response = configService.addDevice(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Unexpected error while adding device {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * 保存OFD系统参数
     *
     * @param ofdParams OFD参数对象，包含：
     *                  - rangeDDeviation: 目标距离偏差
     *                  - azimuthDeviation: 目标方位角偏差
     *                  - elevationDeviation: 目标俯仰角偏差
     * @return 保存结果
     */
    @PostMapping("/saveOFDParameters")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> saveOFDParameters(@RequestBody Map<String, Object> ofdParams) {
        try {
            String rangeDeviation = (String) ofdParams.get("rangeDDeviation");
            String azimuthDeviation = (String) ofdParams.get("azimuthDeviation");
            String elevationDeviation = (String) ofdParams.get("elevationDeviation");

            // 获取当前用户名
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = authentication != null ? authentication.getName() : "system";

            Map<String, Object> result = configService.saveOFDParameters(
                    rangeDeviation, azimuthDeviation, elevationDeviation, currentUser);

            if ((boolean) result.get("success")) {
                log.info("OFD parameters saved successfully by user: {}", currentUser);
                return ResponseEntity.ok(result);
            } else {
                log.warn("Failed to save OFD parameters: {}", result.get("message"));
                return ResponseEntity.internalServerError().body(result);
            }
        } catch (Exception e) {
            log.error("Error saving OFD parameters", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "保存失败: " + e.getMessage()));
        }
    }

    /**
     * 获取OFD系统参数
     *
     * @return OFD参数对象
     */
    @GetMapping("/getOFDParameters")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getOFDParameters() {
        try {
            Map<String, String> ofdParams = configService.getOFDParameters();
            log.info("OFD parameters retrieved successfully");
            return ResponseEntity.ok(Map.of("success", true, "data", ofdParams));
        } catch (Exception e) {
            log.error("Error retrieving OFD parameters", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "获取失败: " + e.getMessage()));
        }
    }

    /**
     * 获取特定的配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    @GetMapping("/getConfigValue")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getConfigValue(@RequestParam String configKey) {
        try {
            String configValue = configService.getConfigValue(configKey);
            if (configValue != null) {
                return ResponseEntity.ok(Map.of("success", true, "configKey", configKey, "configValue", configValue));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "配置不存在"));
            }
        } catch (Exception e) {
            log.error("Error retrieving config value for key: {}", configKey, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "获取失败: " + e.getMessage()));
        }
    }


    @PostMapping("/getMapConfig")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public Object getMapConfig() {
        try {
            List<String> keys = List.of("sys.map.defaultScale", "sys.map.centerPoint", "sys.zone.detection", "sys.zone.warning", "sys.zone.countermeasure");
            List<Config> configKeys = configService.getConfigKeys(keys);


            Map<String, Object> result = new HashMap<>();
            configKeys.stream()
                    .collect(Collectors.groupingBy(Config::getConfigKey))
                    .forEach((k, v) -> {
                        Config first = v.getFirst();
                        String configValue = first.getConfigValue();
                        if (StringUtils.isEmpty(configValue)) {
                            return;
                        }
                        switch (k) {
                            case "sys.map.defaultScale":
                                // 缩放比例
                                result.put("mapDefaultScale", Integer.valueOf(configValue));
                                break;
                            case "sys.map.centerPoint":
                                // 地图中心
                                result.put("centerPoint", configValue.split(","));
                                break;
                            case "sys.zone.detection":
                                // 反制区坐标集合
                                result.put("detection", JSON.parseArray(configValue));
                                break;
                            case "sys.zone.warning":
                                // 反制区坐标集合
                                result.put("warning", JSON.parseArray(configValue));
                                break;
                            case "sys.zone.countermeasure":
                                // 反制区坐标集合
                                result.put("countermeasure", JSON.parseArray(configValue));
                                break;
                        }
                    });

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to read configuration {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Failed to read configuration: " + e.getMessage());
        }
    }


    @GetMapping("/updateConfig")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> updateConfig(String key,String val) {
        try {
            User user = BaseContext.getCurrentId();
            configService.updateConfigValue(key,val,user.getId() + "");
            return ResponseEntity.ok("Configuration updated successfully");
        } catch (Exception e) {
            log.error("Unexpected error while updating configuration: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Unexpected error: " + e.getMessage());
        }
    }


    @GetMapping("/getConfigByKeys")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getConfigByKey(String keys) {
        try {
             if(StringUtils.isNotEmpty(keys)) {
                 List<Config> configKeys = configService.getConfigKeys(List.of(keys.split(",")));
                 Map<String,Config> result = new HashMap<>();
                 configKeys.forEach(e-> result.put(e.getConfigKey(),e));
                 return ResponseEntity.ok(result);
             }
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            log.error("Unexpected error while updating configuration: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Unexpected error: " + e.getMessage());
        }
    }
}


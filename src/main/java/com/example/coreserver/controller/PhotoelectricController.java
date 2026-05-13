package com.example.coreserver.controller;

import com.alibaba.fastjson2.JSONObject;
import com.example.coreserver.entity.Config;
import com.example.coreserver.grpc.common.DeviceId;
import com.example.coreserver.grpc.common.Response;
import com.example.coreserver.grpc.photoelectric.*;
import com.example.coreserver.service.business.DroneStatsService;
import com.example.coreserver.service.business.NorthAngle;
import com.example.coreserver.service.device.ConfigService;
import com.example.coreserver.service.device.PhotoelectricService;
//import com.example.coreserver.service.socket.DataForwardClientHandler;
import com.example.coreserver.service.socket.GuidanceDataClientHandler;
import com.example.coreserver.utils.ConfigUtils;
import com.example.coreserver.vo.GuidanceControlVo;
import com.example.coreserver.vo.RadarAndElectricVo;
import com.google.protobuf.util.JsonFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * 光电控制接口
 * @author: zhanghenan
 */
@RestController
@Tag(name = "PhotoelectricController", description = "光电控制接口")
@RequestMapping("/api/photoelectric")
@Slf4j
public class PhotoelectricController {

    @Autowired
    private DroneStatsService droneStatsService;

    @Autowired
    private PhotoelectricService service;

    @Autowired
    private ConfigService configService;

    @Autowired
    private GuidanceDataClientHandler guidanceDataClientHandler;


//    @Autowired
//    private NorthAngle northAngleservice;

    //    @Operation(summary = "获取光电设备列表")
    @GetMapping("/devices")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getPhotoelectricDevices() {
        try {
            DeviceList list = service.getPhotoelectricDevices();
            log.info("Found {} photoelectric devices", list.getDevicesCount());
            return ResponseEntity.ok(JsonFormat.printer().print(list));
        } catch (Exception e) {
            log.error("Failed to Find photoelectric devices: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 设置目标参数
    @Operation(summary = "设置目标参数")
    @PostMapping("/target")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setTargetParameters(@RequestBody String request) {

        try {
            TargetParameters.Builder builder = TargetParameters.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = service.setTargetParameters(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set target parameters: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set parameters");
        }
    }

    // 设置修正量
    @Operation(summary = "设置修正量")
    @PostMapping("/corrections")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setCorrections(@RequestBody String request) {
        try {
            Corrections.Builder builder = Corrections.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = service.setCorrections(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set corrections: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set corrections");
        }
    }

    // 设置航向参数
    @Operation(summary = "设置航向参数")
    @PostMapping("/navigation")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setNavigationParameters(@RequestBody String request) {
        try {
            NavigationParameters.Builder builder = NavigationParameters.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = service.setNavigationParameters(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set navigation parameters: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set parameters");
        }
    }

    // 伺服控制
    @Operation(summary = "伺服控制")
    @PostMapping("/servo")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> controlServo(@RequestBody String request) {
        try {
            ServoControl.Builder builder = ServoControl.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = service.controlServo(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to control servo: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to control servo");
        }
    }

    // 工作模式控制
    @Operation(summary = "工作模式控制")
    @PostMapping("/workMode")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setWorkMode(@RequestBody String request) {
        try {
            WorkMode.Builder builder = WorkMode.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = service.setWorkMode(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set work mode: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set work mode");
        }
    }

    // 红外电源控制
    @Operation(summary = "红外电源控制")
    @PostMapping("/irPower")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setIRPower(@RequestBody String request) {
        try {
            PowerControl.Builder builder = PowerControl.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = service.setIRPower(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set IR power: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set IR power");
        }
    }

    // 激光电源控制
    @Operation(summary = "激光电源控制")
    @PostMapping("/laserPower")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setLaserPower(@RequestBody String request) {
        try {
            PowerControl.Builder builder = PowerControl.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = service.setLaserPower(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set laser power: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set laser power");
        }
    }

    // 激光能量设定
    @Operation(summary = "激光能量设定")
    @PostMapping("/laserEnergy")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setLaserEnergy(@RequestBody String request) {
        try {
            LaserEnergy.Builder builder = LaserEnergy.newBuilder();
            JsonFormat.parser().merge(request, builder);
            LaserEnergy laserEnergy = builder.build();
            if (laserEnergy.getType() < 1 || laserEnergy.getType() > 3) {
                return ResponseEntity.badRequest().body("Invalid laser energy type");
            }
            Response response = service.setLaserEnergy(laserEnergy);
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set laser energy: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set laser energy");
        }
    }

    // 激光发射/停止
    @Operation(summary = "激光开关")
    @PostMapping("/laser")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> launchOrStop(@RequestBody String request) {
        try {
            LaunchControl.Builder builder = LaunchControl.newBuilder();
            JsonFormat.parser().merge(request, builder);
            LaunchControl launchControl = builder.build();
            log.info("Received laser launch control request: deviceId={}, isLaunch={}",
                    launchControl.getDeviceId(),
                    launchControl.getIsLaunch());
            Response response = service.launchOrStop(launchControl);
            log.info("Laser launch control successful for device: {}", launchControl.getDeviceId());
            return ResponseEntity.ok().body(JSONObject.parseObject(JsonFormat.printer().print(response)));
        } catch (Exception e) {
            log.error("Failed to control laser launch for device: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to control laser");
        }
    }

    // 归零控制
    @Operation(summary = "归零控制")
    @PostMapping("/zero")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setZeroPosition(@RequestBody String deviceId) {
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(deviceId, builder);
            Response response = service.setZeroPosition(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set zero position: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set zero position");
        }
    }

    // 跟踪控制
    @Operation(summary = "跟踪控制")
    @PostMapping("/tracking")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setTrackingMode(@RequestBody String request) {
        try {
            TrackingMode.Builder builder = TrackingMode.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = service.setTrackingMode(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set tracking mode: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set tracking mode");
        }
    }

    // 捕获/待机控制
    @Operation(summary = "捕获/待机控制")
    @PostMapping("/capture")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setCaptureMode(@RequestBody String request) {
        try {
            CaptureMode.Builder builder = CaptureMode.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = service.setCaptureMode(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set capture mode: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set capture mode");
        }
    }



    // 雷达引导控制
    @Operation(summary = "雷达引导控制")
    @PostMapping("/radarguidance")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setRadarGuidanceMode(@RequestBody RadarAndElectricVo radarAndElectricVo) {
        String deviceId = radarAndElectricVo.getDeviceId();
        int targetDistance = radarAndElectricVo.getTargetDistance();
        double targetAzimuth = radarAndElectricVo.getTargetAzimuth();
        double targetElevation = radarAndElectricVo.getTargetElevation();
        // 从数据库config表获取修正量值
        List<Config> configKeys = configService.getConfigKeys(List.of(
                "sys.OFD.rangeDDeviation",
                "sys.OFD.azimuthDeviation",
                "sys.OFD.elevationDeviation",
                "sys.OFD.videoRecordingDuration"));

        Config rangeDDeviation = ConfigUtils.getConfig.apply("sys.OFD.rangeDDeviation", configKeys);
        Config azimuthDeviation = ConfigUtils.getConfig.apply("sys.OFD.azimuthDeviation", configKeys);
        Config elevationDeviation = ConfigUtils.getConfig.apply("sys.OFD.elevationDeviation", configKeys);
        Config videoRecordingDuration = ConfigUtils.getConfig.apply("sys.OFD.videoRecordingDuration", configKeys);

        int targetDistance_correction = (int) Double.parseDouble(rangeDDeviation == null ? "0" : rangeDDeviation.getConfigValue());
        double targetAzimuth_correction = Double.parseDouble(azimuthDeviation == null ? "0" : rangeDDeviation.getConfigValue());
        double targetElevation_correction = Double.parseDouble(elevationDeviation == null ? "0" : rangeDDeviation.getConfigValue());
        int a = targetDistance + targetDistance_correction;
        double b = targetAzimuth + targetAzimuth_correction;
        double c = targetElevation - targetElevation_correction;
        try {
            RadarGuidanceParameters.Builder builder = RadarGuidanceParameters.newBuilder();

            // 设置关键参数
            builder.setDeviceId(deviceId)                    // 引导设备
                    .setTargetDistance(a)        // 距离
                    .setTargetAzimuth((float) b)  // 方位角（需转换为float）
                    .setTargetElevation((float) c)
                    .setVideoRecordingDuration(Integer.valueOf(videoRecordingDuration.getConfigValue())); // 高度/仰角

            Response response = service.setRadarGuidanceMode(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to set radar guidance mode: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set radar guidance mode");
        }
    }

    // 融合引导控制
    @Operation(summary = "融合引导控制")
    @PostMapping("/mixguidance")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setMixGuidanceMode(@RequestBody RadarAndElectricVo radarAndElectricVo) {
//        float northangle = northAngleservice.getNorthAngle();
        String deviceId = radarAndElectricVo.getDeviceId();
        int targetDistance = radarAndElectricVo.getTargetDistance();
        double targetAzimuth = radarAndElectricVo.getTargetAzimuth();
        if (targetAzimuth < -180.0) {
            targetAzimuth = targetAzimuth + 360.0;
        }
        double targetElevation = radarAndElectricVo.getTargetElevation();


        List<Config> configKeys = configService.getConfigKeys(List.of(
                "sys.OFD.rangeDDeviation",
                "sys.OFD.azimuthDeviation",
                "sys.OFD.elevationDeviation",
                "sys.OFD.videoRecordingDuration",
                "sys.radar.northAngle"));

        // 从数据库config表获取修正量值
//        int targetDistance_correction = (int) Double.parseDouble(configService.getConfigValue("sys.OFD.rangeDDeviation") == null ? "0" : configService.getConfigValue("sys.OFD.rangeDDeviation"));
//        double targetAzimuth_correction = Double.parseDouble(configService.getConfigValue("sys.OFD.azimuthDeviation") == null ? "0" : configService.getConfigValue("sys.OFD.azimuthDeviation"));
//        double targetElevation_correction = Double.parseDouble(configService.getConfigValue("sys.OFD.elevationDeviation") == null ? "0" : configService.getConfigValue("sys.OFD.elevationDeviation"));

        Config rangeDDeviation = ConfigUtils.getConfig.apply("sys.OFD.rangeDDeviation", configKeys);
        Config azimuthDeviation = ConfigUtils.getConfig.apply("sys.OFD.azimuthDeviation", configKeys);
        Config elevationDeviation = ConfigUtils.getConfig.apply("sys.OFD.elevationDeviation", configKeys);
        Config videoRecordingDuration = ConfigUtils.getConfig.apply("sys.OFD.videoRecordingDuration", configKeys);
        Config northAngle = ConfigUtils.getConfig.apply("sys.radar.northAngle", configKeys);

        int targetDistance_correction = (int) Double.parseDouble(rangeDDeviation == null ? "0" : rangeDDeviation.getConfigValue());
        double targetAzimuth_correction = Double.parseDouble(azimuthDeviation == null ? "0" : rangeDDeviation.getConfigValue());
        double targetElevation_correction = Double.parseDouble(elevationDeviation == null ? "0" : rangeDDeviation.getConfigValue());
        double northAngleVal = Double.parseDouble(northAngle == null ? "0" : northAngle.getConfigValue());

        try {
            MixGuidanceParameters.Builder builder = MixGuidanceParameters.newBuilder();
//            System.out.println("northangle"+northangle);
//            System.out.println((float) targetAzimuth+targetAzimuth_correction+(360.0-northangle+1.5));
            // 设置关键参数
            builder.setDeviceId(deviceId)                    // 引导设备
                    .setTargetDistance(targetDistance + targetDistance_correction)        // 距离
                    .setTargetAzimuth((float) targetAzimuth + targetAzimuth_correction+ (360.0 - northAngleVal))  // 方位角（需转换为float）
                    .setTargetElevation((float) targetElevation - targetElevation_correction); // 高度/仰角

            Response response = service.setMixGuidanceMode(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to set mix guidance mode: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set mix guidance mode");
        }
    }

    // 取消雷达/电侦引导控制
    @Operation(summary = "取消引导")
    @PostMapping("/guidance/cancel")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> cancelGuidanceMode(@RequestBody String deviceId) {
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(deviceId, builder);
            Response response = service.cancelGuidanceMode(builder.build());
            // 处置无人机数量加一
            droneStatsService.incrementDisposedDroneCount();
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to cancel guidance mode: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to cancel guidance mode");
        }
    }

    // 操纵控制
    @Operation(summary = "操纵控制")
    @PostMapping("/manipulate")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> manipulate(@RequestBody String request) {
        try {
            ManipulateCommand.Builder builder = ManipulateCommand.newBuilder();
            JsonFormat.parser().merge(request, builder);
            ManipulateCommand manipulateCommand = builder.build();

            if (manipulateCommand.getType() < 1 || manipulateCommand.getType() > 15) {
                return ResponseEntity.badRequest().body("Invalid manipulation type");
            }

            Response response = service.manipulate(manipulateCommand);

            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set manipulation: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set manipulation");
        }
    }

    //手压跟踪
    @Operation(summary = "手压跟踪")
    @PostMapping("/manualtracking")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> trackerManualTracking(
            @RequestBody String request) {
        try {
            ManualTrackingCommand.Builder builder = ManualTrackingCommand.newBuilder();
            JsonFormat.parser().merge(request, builder);
            ManualTrackingCommand manualTrackingCommand = builder.build();

//            if (manualTrackingCommand.getX() < 1 || manualTrackingCommand.getY() > 9) {
//                return ResponseEntity.badRequest().body("Invalid manualTracking type");
//            }

            Response response = service.trackerManualTracking(manualTrackingCommand);

            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set manualTracking: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set manipulation");
        }
    }


    @Operation(summary = "通道控制")
    @PostMapping("/channel")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setChannel(@RequestBody String request) {
        try {
            ChannelControl.Builder builder = ChannelControl.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = service.setChannel(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set channel: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set channel");
        }
    }

    @Operation(summary = "目标极性控制")
    @PostMapping("/targetPolarity")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setTargetPolarity(@RequestBody String request) {
        try {
            PolarityControl.Builder builder = PolarityControl.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = service.setTargetPolarity(builder.build());

            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set target polarity: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set target polarity");
        }
    }

    @Operation(summary = "红外极性控制")
    @PostMapping("/irPolarity")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setIRPolarity(@RequestBody String request) {
        try {
            PolarityControl.Builder builder = PolarityControl.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = service.setIRPolarity(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set IR polarity: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set IR polarity");
        }
    }

    // 设置待机
    @Operation(summary = "设置待机")
    @PostMapping("/standby")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setStandby(@RequestBody String deviceId) {
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(deviceId, builder);
            Response response = service.setStandby(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to set standby: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set standby");
        }
    }

    // 单次自检 （延用单次自检的api接口名称，但是前端对应按钮现在为 电视/红外自动聚焦）
    @Operation(summary = "自动聚焦")
    @PostMapping("/selfcheck")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> singleSelfCheck(@RequestBody String deviceId) {
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(deviceId, builder);
            Response response = service.singleSelfCheck(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to single self check: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to single self check");
        }
    }

    // 目标识别开关
    @Operation(summary = "目标识别开关")
    @PostMapping("/targetrecognition")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> setTargetRecognition(@RequestBody String request) {
        try {
            RecognitionControl.Builder builder = RecognitionControl.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = service.setTargetRecognition(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Failed to control servo: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to control servo");
        }
    }

    // 获取指定光电设备的实时数据
//    @Operation(summary = "获取指定光电设备的实时数据")
    @GetMapping("/realtime-data/{deviceId}")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getAngleData(@PathVariable String deviceId) {
        try {
            AngleRequest angleRequest = AngleRequest.newBuilder().setDeviceId(deviceId).build();
            AngleData angleData = service.getAngleData(angleRequest);
            String json = JsonFormat.printer().print(angleData);
            return ResponseEntity.ok().body(json);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }


    @Operation(summary = "雷达/TDOA/融合数据的引导控制")
    @PostMapping("/guidance/control")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> guidanceControl(@RequestBody GuidanceControlVo guidanceControlVo) {
        guidanceDataClientHandler.guidanceControl(guidanceControlVo);
        return ResponseEntity.ok().body("操作成功");
    }
}

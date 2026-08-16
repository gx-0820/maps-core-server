package com.example.coreserver.controller;

import com.example.coreserver.grpc.common.DeviceId;
import com.example.coreserver.grpc.common.Response;
import com.example.coreserver.grpc.talent.*;
import com.example.coreserver.service.device.TalentService;
import com.google.protobuf.util.JsonFormat;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deception")
@Slf4j
@Tag(name = "DeceptionController", description = "诱骗控制接口")
public class DeceptionController {

    @Autowired
    private TalentService talentService;

    @Autowired
    private OperationSseController operationSseController;

    @GetMapping("/devices")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getTalentDevices() {
        sendOperationStart("开始获取设备列表");
        log.info("导航诱骗设备，下发【获取设备列表】指令");
        try {
            DeviceListResponse talentDevices = talentService.getTalentDevices();
            sendOperationSuccess("获取设备列表完成，设备数=" + talentDevices.getDevicesCount());
            log.info("导航诱骗设备，下发【获取设备列表】指令，设备数量:{}", talentDevices.getDevicesCount());
            return ResponseEntity.ok(JsonFormat.printer().print(talentDevices));
        } catch (Exception e) {
            sendOperationFailure("获取设备列表");
            log.error("Error fetching devices: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/updateConnect")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> updateConnectSetting(@RequestBody String request) {
        sendOperationStart("开始下发连接参数");
        log.info("导航诱骗设备，下发【updateConnect】指令，请求参数:{}", request);
        try {
            DeceptionRequest.Builder builder = DeceptionRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.UpdateConnectSetting(builder.build());
            sendOperationSuccess("连接参数下发完成");
            log.info("导航诱骗设备，下发【updateConnect】指令，响应结果:{}", response);
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            sendOperationFailure("连接参数下发");
            log.error("Error fetching connect setting: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/updateCommand")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> updateCommand(@RequestBody String request) {
        sendOperationStart("开始下发控制命令");
        log.info("导航诱骗设备，下发【updateCommand】指令，请求参数:{}", request);
        try {
            DeceptionRequest.Builder builder = DeceptionRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.UpdateCommand(builder.build());
            sendOperationSuccess("控制命令下发完成");
            log.info("导航诱骗设备，下发【updateCommand】指令，响应结果:{}", response);
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            sendOperationFailure("控制命令下发");
            log.error("Error fetching command: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/getSimulationStatus")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getSimulationStatus(@RequestBody String request) {
        sendOperationStart("开始查询模拟状态");
        log.info("导航诱骗设备，下发【获取模拟状态】指令，请求参数:{}", request);
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(request, builder);
            SimulationStatus simulationStatus = talentService.getSimulationStatus(builder.build());
            sendOperationSuccess("模拟状态查询完成");
            log.info("导航诱骗设备，下发【获取模拟状态】指令，响应结果:{}", simulationStatus);
            return ResponseEntity.ok(JsonFormat.printer().print(simulationStatus));
        } catch (Exception e) {
            sendOperationFailure("模拟状态查询");
            log.error("Error fetching simulation status: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/isConnected")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> isConnected(@RequestBody String request) {
        sendOperationStart("开始查询连接状态");
        log.info("导航诱骗设备，下发【检查连接状态】指令，请求参数:{}", request);
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(request, builder);
            ConnectionStatus connected = talentService.isConnected(builder.build());
            sendOperationSuccess("连接状态查询完成，结果=" + (connected.getConnected() ? "已连接" : "未连接"));
            log.info("导航诱骗设备，下发【检查连接状态】指令，响应结果:{}", connected);
            return ResponseEntity.ok(JsonFormat.printer().print(connected));
        } catch (Exception e) {
            sendOperationFailure("连接状态查询");
            log.error("Error fetching connection status: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/bootstrapPosition")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> sendBootstrapPosition(@RequestBody String request) {
        sendOperationStart("开始下发引导位置");
        log.info("导航诱骗设备，下发【引导位置命令】指令，请求参数:{}", request);
        try {
            PositionRequest.Builder builder = PositionRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.sendBootstrapPositionCommand(builder.build());
            sendOperationSuccess("引导位置下发完成");
            log.info("导航诱骗设备，下发【引导位置命令】指令，响应结果:{}", response);
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            sendOperationFailure("引导位置下发");
            log.error("Error sending bootstrap position: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/capture")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> sendCapture(@RequestBody String request) {
        sendOperationStart("开始下发捕获指令");
        log.info("导航诱骗设备，下发【捕获】指令，请求参数:{}", request);
        try {
            CaptureRequest.Builder builder = CaptureRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.sendCaptureCommand(builder.build());
            sendOperationSuccess("捕获指令下发完成");
            log.info("导航诱骗设备，下发【捕获】指令，响应结果:{}", response);
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            sendOperationFailure("捕获指令下发");
            log.error("Error sending capture command: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/defense")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> sendDefense(@RequestBody String request) {
        sendOperationStart("开始下发防御指令");
        log.info("导航诱骗设备，下发【防御】指令，请求参数:{}", request);
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.sendDefenseCommand(builder.build());
            sendOperationSuccess("防御指令下发完成");
            log.info("导航诱骗设备，下发【防御】指令，响应结果:{}", response);
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            sendOperationFailure("防御指令下发");
            log.error("Error sending defense command: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/driveAngle")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> sendDriveAngle(@RequestBody String request) {
        sendOperationStart("开始下发驱离角度");
        log.info("导航诱骗设备，下发【驱离角度】指令，请求参数:{}", request);
        try {
            DriveAngleRequest.Builder builder = DriveAngleRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.sendDriveAngleCommand(builder.build());
            sendOperationSuccess("驱离角度下发完成");
            log.info("导航诱骗设备，下发【驱离角度】指令，响应结果:{}", response);
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            sendOperationFailure("驱离角度下发");
            log.error("Error sending drive angle command: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/interference")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> sendInterference(@RequestBody String request) {
        sendOperationStart("开始下发干扰指令");
        log.info("导航诱骗设备，下发【干扰】指令，请求参数:{}", request);
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.sendInterferenceCommand(builder.build());
            sendOperationSuccess("干扰指令下发完成");
            log.info("导航诱骗设备，下发【干扰】指令，响应结果:{}", response);
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            sendOperationFailure("干扰指令下发");
            log.error("Error sending interference command: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/noFly")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> sendNoFly(@RequestBody String request) {
        sendOperationStart("开始下发禁飞指令");
        log.info("导航诱骗设备，下发【禁飞】指令，请求参数:{}", request);
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.sendNoFly(builder.build());
            sendOperationSuccess("禁飞指令下发完成");
            log.info("导航诱骗设备，下发【禁飞】指令，响应结果:{}", response);
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            sendOperationFailure("禁飞指令下发");
            log.error("Error sending no-fly command: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/transmitPower")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> sendTransmitPower(@RequestBody String request) {
        sendOperationStart("开始下发功率设置");
        log.info("导航诱骗设备，下发【设置功率】指令，请求参数:{}", request);
        try {
            TransmitPowerRequest.Builder builder = TransmitPowerRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.sendTransmitPowerCommand(builder.build());
            sendOperationSuccess("功率设置下发完成");
            log.info("导航诱骗设备，下发【设置功率】指令，响应结果:{}", response);
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            sendOperationFailure("功率设置下发");
            log.error("Error sending transmit power command: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/stopLaunch")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> stopLaunch(@RequestBody String request) {
        sendOperationStart("开始下发取消指令");
        log.info("导航诱骗设备，下发【取消】指令，请求参数:{}", request);
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.stopLaunch(builder.build());
            sendOperationSuccess("取消指令下发完成");
            log.info("导航诱骗设备，下发【取消】指令，响应结果:{}", response);
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            sendOperationFailure("取消指令下发");
            log.error("Error stopping launch: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private void sendOperationStart(String message) {
        operationSseController.sendOperation("导航诱骗：" + message);
    }

    private void sendOperationSuccess(String message) {
        operationSseController.sendOperation("导航诱骗：" + message);
    }

    private void sendOperationFailure(String action) {
        operationSseController.sendOperation("导航诱骗：" + action + "失败");
    }
}

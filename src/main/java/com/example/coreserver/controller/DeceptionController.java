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

    @GetMapping("/devices")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getTalentDevices() {
        try {
            DeviceListResponse talentDevices = talentService.getTalentDevices();
            log.info("Found {} devices", talentDevices.getDevicesCount());
            return ResponseEntity.ok(JsonFormat.printer().print(talentDevices));
        } catch (Exception e) {
            log.error("Error fetching devices: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/updateConnect")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> updateConnectSetting(@RequestBody String request) {
        try {
            DeceptionRequest.Builder builder = DeceptionRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.UpdateConnectSetting(builder.build());
            return ResponseEntity.ok().body(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Error fetching connect setting: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/updateCommand")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> updateCommand(@RequestBody String request) {
        try {
            DeceptionRequest.Builder builder = DeceptionRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.UpdateCommand(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Error fetching command: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/getSimulationStatus")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getSimulationStatus(@RequestBody String request) {
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(request, builder);
            SimulationStatus simulationStatus = talentService.getSimulationStatus(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(simulationStatus));
        } catch (Exception e) {
            log.error("Error fetching simulation status: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/isConnected")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> isConnected(@RequestBody String request) {
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(request, builder);
            ConnectionStatus connected = talentService.isConnected(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(connected));
        } catch (Exception e) {
            log.error("Error fetching connection status: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/bootstrapPosition")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> sendBootstrapPosition(@RequestBody String request) {
        try {
            PositionRequest.Builder builder = PositionRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.sendBootstrapPositionCommand(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Error sending bootstrap position: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/capture")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> sendCapture(@RequestBody String request) {
        try {
            CaptureRequest.Builder builder = CaptureRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.sendCaptureCommand(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Error sending capture command: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/defense")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> sendDefense(@RequestBody String request) {
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.sendDefenseCommand(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Error sending defense command: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/driveAngle")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> sendDriveAngle(@RequestBody String request) {
        try {
            DriveAngleRequest.Builder builder = DriveAngleRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.sendDriveAngleCommand(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Error sending drive angle command: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/interference")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> sendInterference(@RequestBody String request) {
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.sendInterferenceCommand(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Error sending interference command: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/noFly")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> sendNoFly(@RequestBody String request) {
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.sendNoFly(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Error sending no-fly command: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/transmitPower")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> sendTransmitPower(@RequestBody String request) {
        try {
            TransmitPowerRequest.Builder builder = TransmitPowerRequest.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.sendTransmitPowerCommand(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Error sending transmit power command: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/stopLaunch")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> stopLaunch(@RequestBody String request) {
        try {
            DeviceId.Builder builder = DeviceId.newBuilder();
            JsonFormat.parser().merge(request, builder);
            Response response = talentService.stopLaunch(builder.build());
            return ResponseEntity.ok(JsonFormat.printer().print(response));
        } catch (Exception e) {
            log.error("Error stopping launch: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}

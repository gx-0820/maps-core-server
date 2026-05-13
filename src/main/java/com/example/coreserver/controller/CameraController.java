package com.example.coreserver.controller;

import com.example.coreserver.grpc.camera.DeviceListResponse;
import com.example.coreserver.service.device.CameraService;
import com.google.protobuf.util.JsonFormat;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 摄像头控制接口
 * @author: zhanghenan
 */
@Slf4j
@RestController
@RequestMapping("/api/camera")
@Tag(name = "CameraController", description = "摄像头控制接口")
public class CameraController {

    @Autowired
    private CameraService cameraService;

    @GetMapping("/cameras")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getCameras() {
        try {
            DeviceListResponse cameraDevices = cameraService.getCameraDevices();
            log.info("Found {} camera devices", cameraDevices.getDevicesCount());
            return ResponseEntity.ok(JsonFormat.printer().print(cameraDevices));
        } catch (Exception e) {
            log.error("Failed to get cameras: {}",e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to get cameras");
        }
    }

}

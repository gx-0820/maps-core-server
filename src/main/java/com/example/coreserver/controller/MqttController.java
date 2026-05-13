package com.example.coreserver.controller;

import com.alibaba.fastjson2.JSONObject;
import com.example.coreserver.config.MqttConfig;
import com.example.coreserver.service.device.SilasService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mqtt")
@Slf4j
@Tag(name = "MqttController", description = "Silas控制接口")
public class MqttController {

    @Autowired
    private MqttConfig mqttConfig;

    @Autowired
    private SilasService silasService;

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(mqttConfig.getStatus());
    }

    @PostMapping("/status")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> updateStatus(@RequestBody String request) {
        JSONObject jsonObject = JSONObject.parseObject(request);
        int status = jsonObject.getIntValue("status");
        if (status != 0 && status != 1) {
            return ResponseEntity.badRequest().body("Invalid status value! Must be 0 or 1.");
        }
        if (status == 1) {
            try {
                silasService.init();
            } catch (MqttException e) {
                log.error("Error initializing MQTT client: ", e);
                return ResponseEntity.internalServerError().body("Error initializing MQTT client.");
            }
        }
        if (status == 0) {
            try {
                silasService.close();
            } catch (MqttException e) {
                log.error("Error closing MQTT client: ", e);
                return ResponseEntity.internalServerError().body("Error closing MQTT client.");
            }
        }
        mqttConfig.setStatus(status);
        return ResponseEntity.ok("MQTT status updated to " + status);
    }
}

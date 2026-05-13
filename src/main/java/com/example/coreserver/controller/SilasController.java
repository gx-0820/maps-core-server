package com.example.coreserver.controller;

import com.alibaba.fastjson2.JSONObject;
import com.example.coreserver.config.SilasConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/silas")
@Slf4j
@Tag(name = "SilasController", description = "Silas控制接口")
public class SilasController {
    @Autowired
    private SilasConfig silasConfig;

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getStatus() {
        Map<String,String> map = Map.of(
                "broker", silasConfig.getBroker(),
                "topic", silasConfig.getTopic(),
                "username", silasConfig.getUsername(),
                "password", silasConfig.getPassword()
        );
        return ResponseEntity.ok(map);
    }


    @PostMapping("/all")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> updateAll(@RequestBody String request) {
        JSONObject jsonObject = JSONObject.parseObject(request);
        String broker = jsonObject.getString("broker");
        String topic = jsonObject.getString("topic");
        String username = jsonObject.getString("username");
        String password = jsonObject.getString("password");

        silasConfig.setBroker(broker);
        silasConfig.setTopic(topic);
        silasConfig.setUsername(username);
        silasConfig.setPassword(password);

        return ResponseEntity.ok("Silas configuration updated successfully.");
    }


    @PostMapping("/broker")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> updateBroker(@RequestBody String request) {
        JSONObject jsonObject = JSONObject.parseObject(request);
        String broker = jsonObject.getString("broker");
        silasConfig.setBroker(broker);
        return ResponseEntity.ok("Silas broker updated to: " + broker);
    }

    @PostMapping("/topic")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> updateTopic(@RequestBody String request) {
        JSONObject jsonObject = JSONObject.parseObject(request);
        String topic = jsonObject.getString("topic");
        silasConfig.setTopic(topic);
        return ResponseEntity.ok("Silas topic updated to: " + topic);
    }

    @PostMapping("/username")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> updateUsername(@RequestBody String request) {
        JSONObject jsonObject = JSONObject.parseObject(request);
        String username = jsonObject.getString("username");
        silasConfig.setUsername(username);
        return ResponseEntity.ok("Silas username updated to: " + username);
    }

    @PostMapping("/password")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> updatePassword(@RequestBody String request) {
        JSONObject jsonObject = JSONObject.parseObject(request);
        String password = jsonObject.getString("password");
        silasConfig.setPassword(password);
        return ResponseEntity.ok("Silas password updated to: " + password);
    }
}

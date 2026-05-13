package com.example.coreserver.controller;



import com.example.coreserver.service.business.AlertRulesService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/alert")
@Api(tags = "预警规则引擎接口")
public class AlertRulesController {
    @Autowired
    AlertRulesService alertRulesService;

    @GetMapping("/warningLevel")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getWarningLevel() {
        int level = alertRulesService.getWarningLevel();
        return ResponseEntity.ok().body(level);
    }


    @GetMapping("/warningStrategy")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> getWarningStrategy() {
        alertRulesService.getWarningStrategy();
        return null;
    }




}

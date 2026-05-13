package com.example.coreserver.controller;

import com.example.coreserver.common.RespCodeEnum;
import com.example.coreserver.common.Result;
import com.example.coreserver.service.business.TargetMonitorStatsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/drone-stats")
@Tag(name = "DroneStatsController", description = "监控目标统计接口")
public class DroneStatsController {

    private final TargetMonitorStatsService targetMonitorStatsService;

    public DroneStatsController(TargetMonitorStatsService targetMonitorStatsService) {
        this.targetMonitorStatsService = targetMonitorStatsService;
    }

    @GetMapping("/daily")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<Result> getDailyDroneStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            return ResponseEntity.ok(Result.success(targetMonitorStatsService.getDailyStats(date)));
        } catch (Exception ex) {
            log.error("Failed to query daily drone stats", ex);
            return ResponseEntity.internalServerError().body(Result.error(RespCodeEnum.SERVER_ERROR, ex.getMessage()));
        }
    }

    @GetMapping("/trend")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<Result> getDroneStatsTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            return ResponseEntity.ok(Result.success(targetMonitorStatsService.getTrendStats(endDate)));
        } catch (Exception ex) {
            log.error("Failed to query drone stats trend", ex);
            return ResponseEntity.internalServerError().body(Result.error(RespCodeEnum.SERVER_ERROR, ex.getMessage()));
        }
    }
}

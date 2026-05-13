package com.example.coreserver.controller;

import com.example.coreserver.common.RespCodeEnum;
import com.example.coreserver.common.Result;
import com.example.coreserver.service.business.TargetQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@RestController
@RequestMapping("/api/targets")
@Tag(name = "TargetController", description = "原始目标查询接口")
public class TargetController {

    private final TargetQueryService targetQueryService;

    public TargetController(TargetQueryService targetQueryService) {
        this.targetQueryService = targetQueryService;
    }

    @GetMapping("/radar")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<Result> getRadarTargetList(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
            @RequestParam(required = false) Integer targetType,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        try {
            return ResponseEntity.ok(Result.success(
                    targetQueryService.pageRadarTargets(date, startTime, endTime, targetType, pageNum, pageSize)));
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid radar target list request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Result.error(RespCodeEnum.REQUEST_ERROR, ex.getMessage()));
        } catch (Exception ex) {
            log.error("Failed to query radar target list", ex);
            return ResponseEntity.internalServerError().body(Result.error(RespCodeEnum.SERVER_ERROR, ex.getMessage()));
        }
    }

    @GetMapping("/radar/{targetId}/trajectory")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<Result> getRadarTargetTrajectory(
            @PathVariable String targetId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        try {
            return ResponseEntity.ok(Result.success(targetQueryService.pageRadarTrajectory(targetId, pageNum, pageSize)));
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid radar trajectory request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Result.error(RespCodeEnum.REQUEST_ERROR, ex.getMessage()));
        } catch (Exception ex) {
            log.error("Failed to query radar target trajectory", ex);
            return ResponseEntity.internalServerError().body(Result.error(RespCodeEnum.SERVER_ERROR, ex.getMessage()));
        }
    }

    @GetMapping("/radar/{targetId}/OFDvideo")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<Result> getRadarTargetOFDVideo(
            @PathVariable String targetId) {
        try {
            return ResponseEntity.ok(Result.success(targetQueryService.getRadarTargetOFDVideo(targetId)));
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid radar trajectory request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Result.error(RespCodeEnum.REQUEST_ERROR, ex.getMessage()));
        } catch (Exception ex) {
            log.error("Failed to query radar target trajectory", ex);
            return ResponseEntity.internalServerError().body(Result.error(RespCodeEnum.SERVER_ERROR, ex.getMessage()));
        }
    }

    @GetMapping("/tdoa")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<Result> getTdoaTargetList(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
            @RequestParam(required = false) Integer targetType,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        try {
            return ResponseEntity.ok(Result.success(
                    targetQueryService.pageTdoaTargets(date, startTime, endTime, targetType, pageNum, pageSize)));
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid TDOA target list request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Result.error(RespCodeEnum.REQUEST_ERROR, ex.getMessage()));
        } catch (Exception ex) {
            log.error("Failed to query TDOA target list", ex);
            return ResponseEntity.internalServerError().body(Result.error(RespCodeEnum.SERVER_ERROR, ex.getMessage()));
        }
    }

    @GetMapping("/tdoa/{targetId}/trajectory")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<Result> getTdoaTargetTrajectory(
            @PathVariable String targetId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        try {
            return ResponseEntity.ok(Result.success(targetQueryService.pageTdoaTrajectory(targetId, pageNum, pageSize)));
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid TDOA trajectory request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Result.error(RespCodeEnum.REQUEST_ERROR, ex.getMessage()));
        } catch (Exception ex) {
            log.error("Failed to query TDOA target trajectory", ex);
            return ResponseEntity.internalServerError().body(Result.error(RespCodeEnum.SERVER_ERROR, ex.getMessage()));
        }
    }

    @GetMapping("/fusion")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<Result> getFusionTargetList(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
            @RequestParam(required = false) String targetType,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        try {
            return ResponseEntity.ok(Result.success(
                    targetQueryService.pageFusionTargets(date, startTime, endTime, targetType, pageNum, pageSize)));
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid fusion target list request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Result.error(RespCodeEnum.REQUEST_ERROR, ex.getMessage()));
        } catch (Exception ex) {
            log.error("Failed to query fusion target list", ex);
            return ResponseEntity.internalServerError().body(Result.error(RespCodeEnum.SERVER_ERROR, ex.getMessage()));
        }
    }

    @GetMapping("/fusion/{targetId}/trajectory")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<Result> getFusionTargetTrajectory(
            @PathVariable String targetId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        try {
            return ResponseEntity.ok(Result.success(targetQueryService.pageFusionTrajectory(targetId, pageNum, pageSize)));
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid fusion trajectory request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Result.error(RespCodeEnum.REQUEST_ERROR, ex.getMessage()));
        } catch (Exception ex) {
            log.error("Failed to query fusion target trajectory", ex);
            return ResponseEntity.internalServerError().body(Result.error(RespCodeEnum.SERVER_ERROR, ex.getMessage()));
        }
    }
}

package com.example.coreserver.controller;

import com.example.coreserver.service.algorithm.DataFusionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据融合接口
 */
@Slf4j
@RestController
@RequestMapping("/api/data-fusion")
@Tag(name = "DataFusionController", description = "数据融合接口")
public class DataFusionController {

    private final DataFusionService dataFusionService;

    @Autowired
    public DataFusionController(DataFusionService dataFusionService) {
        this.dataFusionService = dataFusionService;
    }

    /**
     * 获取数据融合列表
     * @return 数据融合列表
     */
//    @Operation(summary = "获取数据融合列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<List<Map<String, Object>>> getDataFusionList() {
        try {
            List<Map<String, Object>> dataList = dataFusionService.getDataFusionList();
            log.info("返回 {} 条数据融合记录", dataList.size());
            return ResponseEntity.ok(dataList);
        } catch (Exception e) {
            log.error("获取数据融合记录失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据目标ID获取数据融合信息
     * @param targetId 目标ID
     * @return 数据融合信息
     */
//    @Operation(summary = "根据目标ID获取数据融合信息")
    @GetMapping("/target/{targetId}")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<List<Map<String, Object>>> getDataFusionByTargetId(@PathVariable Integer targetId) {
        try {
            List<Map<String, Object>> dataList = dataFusionService.getDataFusionByTargetId(targetId);
            log.info("返回目标 {} 的 {} 条数据融合记录", targetId, dataList.size());
            return ResponseEntity.ok(dataList);
        } catch (Exception e) {
            log.error("获取目标 {} 的数据融合记录失败: {}", targetId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 
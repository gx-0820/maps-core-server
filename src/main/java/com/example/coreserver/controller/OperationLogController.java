package com.example.coreserver.controller;

import com.example.coreserver.annotation.OptLog;
import com.example.coreserver.dto.OperationLogConditionDTO;
import com.example.coreserver.service.log.OperationLogService;
import com.example.coreserver.vo.OperationLogVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.baomidou.mybatisplus.core.assist.ISqlRunner.DELETE;

/**
 * @author lord
 * @date 2025/4/4
 * @description 操作日志模块
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "操作日志模块")
@RequestMapping("/api/operation")
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping("/logs")
    @ApiOperation(value = "查看操作日志", httpMethod = "GET", response = ResponseEntity.class, notes = "查看操作日志")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<List<OperationLogVO>> listOperationLogs(OperationLogConditionDTO operationLogConditionDTO) {
        return ResponseEntity.ok(operationLogService.listOperationLogs(operationLogConditionDTO));
    }

    @OptLog(optType = DELETE)
    @DeleteMapping("/logs")
    @ApiOperation(value = "删除操作日志", httpMethod = "DELETE", response = ResponseEntity.class, notes = "查看操作日志")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> deleteOperationLogs(@RequestBody List<Integer> operationLogIds) {
        operationLogService.removeByIds(operationLogIds);
        return ResponseEntity.ok("Success");
    }
}
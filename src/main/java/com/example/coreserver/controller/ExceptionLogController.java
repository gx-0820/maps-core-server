package com.example.coreserver.controller;

import com.example.coreserver.annotation.OptLog;
import com.example.coreserver.dto.ExceptionLogConditionDTO;
import com.example.coreserver.service.log.ExceptionLogService;
import com.example.coreserver.vo.ExceptionLogVO;
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
 * @description 异常日志模块
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "异常日志模块")
@RequestMapping("/api/exception")
public class ExceptionLogController {

    private final ExceptionLogService exceptionLogService;

    @GetMapping("/logs")
    @ApiOperation(value = "获取异常日志", httpMethod = "GET", response = ResponseEntity.class, notes = "获取异常日志")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<List<ExceptionLogVO>> listExceptionLogs(ExceptionLogConditionDTO exceptionLogConditionDTO) {
        return ResponseEntity.ok(exceptionLogService.listExceptionLogs(exceptionLogConditionDTO));
    }

    @OptLog(optType = DELETE)
    @DeleteMapping("/logs")
    @ApiOperation(value = "删除异常日志", httpMethod = "DELETE", response = ResponseEntity.class, notes = "删除异常日志")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<?> deleteExceptionLogs(@RequestBody List<Integer> exceptionLogIds) {
        exceptionLogService.removeByIds(exceptionLogIds);
        return ResponseEntity.ok("success");
    }
}
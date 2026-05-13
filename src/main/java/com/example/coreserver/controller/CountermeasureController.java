package com.example.coreserver.controller;

import com.example.coreserver.dto.CountermeasureRequest;
import com.example.coreserver.dto.ModeUpdateRequest;
import com.example.coreserver.entity.countermeasure.CountermeasureMode;
import com.example.coreserver.service.business.CountermeasureService;
import com.example.coreserver.service.countermeasure.CountermeasureAutoTaskService;
import com.example.coreserver.service.countermeasure.CountermeasureConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 反制策略决策接口
 *
 * @author gaoxin codex
 */
@RestController
@Api(tags = "反制策略决策接口")
@RequestMapping("/api/countermeasure")
@RequiredArgsConstructor
public class CountermeasureController {

    private final CountermeasureConfigService countermeasureConfigService;
    private final CountermeasureAutoTaskService countermeasureAutoTaskService;
    private final CountermeasureService countermeasureService;

    // 处理POST请求，用于更新模式
    @Operation(summary = "模式已更新")
    @PostMapping("/mode")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<String> updateMode(@RequestBody ModeUpdateRequest request) {
        countermeasureConfigService.setMode(request.getMode(), "api");
        if (CountermeasureMode.AUTO.equals(request.getMode())) {
            countermeasureAutoTaskService.triggerImmediateRound();
        } else {
            countermeasureAutoTaskService.stopCurrentIntervention("接口请求切回人工模式");
        }
        return ResponseEntity.ok("模式更新成功");
    }

    // 处理GET请求，用于获取当前模式
    @ApiOperation(value = "获取当前模式")
    @GetMapping("/mode")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<CountermeasureMode> getCurrentMode() {
        return ResponseEntity.ok(countermeasureConfigService.getMode());
    }

    // 处理POST请求，用于处理威胁
    @ApiOperation(value = "处理威胁")
    @PostMapping("/handle")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<String> handleThreat(Boolean model) {
        // 调用服务处理威胁，并返回处理结果
        return ResponseEntity.ok(countermeasureService.handleThreat(null));
    }
}

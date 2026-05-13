package com.example.coreserver.controller;

import com.example.coreserver.dto.*;
import com.example.coreserver.entity.Geofence;
import com.example.coreserver.service.business.GeofenceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author gaoxin
 * @description: 禁飞区管理接口
 */
@RestController
@Api(tags = "禁飞区管理接口")
@RequestMapping("/api/geofences")
public class GeofenceController {
    // 定义一个GeofenceService类型的成员变量，用于调用业务逻辑
    private final GeofenceService service;

    // 构造函数，通过依赖注入的方式初始化GeofenceService
    public GeofenceController(GeofenceService service) {
        this.service = service;
    }

    // 处理POST请求，用于创建新的禁飞区
    @Operation(summary = "创建禁飞区")
    @ApiOperation("创建禁飞区")
    @PostMapping
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<Geofence> create(@Valid @RequestBody CreateGeofenceRequest req) {
        // 调用service的create方法创建新的禁飞区，并返回创建成功的响应
        return ResponseEntity.ok(service.create(req));
    }

    // 处理PUT请求，用于更新指定ID的禁飞区
    @Operation(summary = "更新禁飞区")
    @ApiOperation("更新禁飞区")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<Geofence> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGeofenceRequest req) {
        // 调用service的update方法更新禁飞区，并返回更新成功的响应
        return ResponseEntity.ok(service.update(id, req));
    }

    // 处理GET请求，用于获取所有禁飞区
//    @Operation(summary = "获取所有禁飞区")
    @ApiOperation("获取所有禁飞区")
    @GetMapping
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<List<Geofence>> getAll() {
        // 调用service的findAll方法获取所有禁飞区，并返回查询成功的响应
        return ResponseEntity.ok(service.findAll());
    }

    // 处理DELETE请求，用于删除指定ID的禁飞区
    @Operation(summary = "删除禁飞区")
    @ApiOperation("删除禁飞区")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // 调用service的delete方法删除禁飞区
        service.delete(id);
        // 返回删除成功的响应，不包含任何内容
        return ResponseEntity.ok().build();
    }

    // 处理POST请求，用于导入KML文件
    @Operation(summary = "导入KML文件")
    @ApiOperation("导入KML文件")
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<String> importKml(@RequestParam("file") MultipartFile file) {
        try {
            // 调用service的importKml方法导入KML文件，并返回导入成功的响应
            return ResponseEntity.ok(service.importKml(file));
        } catch (IOException e) {
            // 如果文件解析失败，返回错误响应
            return ResponseEntity.badRequest().body("文件解析失败");
        }
    }

    // 处理GET请求，用于导出KML文件
    @Operation(summary = "导出KML文件")
    @ApiOperation("导出KML文件")
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public ResponseEntity<InputStreamResource> exportKml() {
        try {
            // 调用service的exportKml方法导出KML文件
            byte[] kmlBytes = service.exportKml();
            // 返回导出成功的响应，设置Content-Type和Content-Disposition头
            return ResponseEntity.ok()
                    .header("Content-Type", "application/vnd.google-earth.kml+xml")
                    .header("Content-Disposition", "attachment; filename=geofences.kml")
                    .body(new InputStreamResource(new ByteArrayInputStream(kmlBytes)));
        } catch (IOException e) {
            // 如果导出失败，返回服务器内部错误响应
            return ResponseEntity.internalServerError().build();
        }
    }
}
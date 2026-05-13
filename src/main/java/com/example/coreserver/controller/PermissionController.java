package com.example.coreserver.controller;


import com.example.coreserver.dto.PermDTO;
import com.example.coreserver.dto.PermUDDTO;
import com.example.coreserver.entity.Permission;
import com.example.coreserver.service.role.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhn
 * @since 2025-03-10
 */
@RestController
@RequestMapping("/api/permission")
@Slf4j
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @PostMapping("/add")
    public ResponseEntity<?> addPermission(@RequestBody PermDTO permDTO) {
        Permission permissionByName = permissionService.getPermissionByCode(permDTO.getPermissionCode());
        if (permissionByName != null) {
            return ResponseEntity.badRequest().body("权限已存在");
        }
        Permission permission = new Permission();
        permission.setPermissionName(permDTO.getPermissionName());
        permission.setPermissionCode(permDTO.getPermissionCode());
        permission.setCreateTime(LocalDateTime.now());
        permission.setDeleted(false);
        permission.setStatus(0);
        boolean result = permissionService.save(permission);
        if (result) {
            log.info("添加权限成功: {}", permission);
            return ResponseEntity.ok("添加权限成功");
        } else {
            return ResponseEntity.badRequest().body("添加权限失败");
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePermission(@PathVariable Integer id) {
        Permission permission = permissionService.getById(id);
        if (permission == null) {
            return ResponseEntity.badRequest().body("权限不存在");
        }
        boolean result = permissionService.removeById(permission.getId());
        if (result) {
            log.info("删除权限成功: {}", permission);
            return ResponseEntity.ok("删除权限成功");
        } else {
            return ResponseEntity.badRequest().body("删除权限失败");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updatePermission(@RequestBody PermUDDTO dto) {
        Permission permission = permissionService.getById(dto.getId());
        if (permission == null) {
            return ResponseEntity.badRequest().body("权限不存在");
        }
        permission.setPermissionName(dto.getPermissionName());
        permission.setPermissionCode(dto.getPermissionCode());
        boolean result = permissionService.updateById(permission);
        if (result) {
            log.info("更新权限成功: {}", permission);
            return ResponseEntity.ok("更新权限成功");
        } else {
            return ResponseEntity.badRequest().body("更新权限失败");
        }
    }

}

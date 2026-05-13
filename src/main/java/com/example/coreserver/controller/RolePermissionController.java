package com.example.coreserver.controller;


import com.example.coreserver.dto.RPDTO;
import com.example.coreserver.entity.Permission;
import com.example.coreserver.entity.Role;
import com.example.coreserver.entity.RolePermission;
import com.example.coreserver.service.role.PermissionService;
import com.example.coreserver.service.role.RolePermissionService;
import com.example.coreserver.service.role.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhn
 * @since 2025-03-10
 */
@RestController
@RequestMapping("/api/rolePermission")
@Slf4j
public class RolePermissionController {

    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RoleService roleService;

    @PostMapping("/add")
    public ResponseEntity<?> addRolePermission(@RequestBody RPDTO dto) {
        RolePermission byId = rolePermissionService.getRolePermissionById(dto.getRoleId(), dto.getPermissionId());
        if (byId != null) {
            return ResponseEntity.badRequest().body("该角色权限已存在");
        }
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(dto.getRoleId());
        rolePermission.setPermissionId(dto.getPermissionId());
        boolean save = rolePermissionService.save(rolePermission);
        if (!save) {
            return ResponseEntity.badRequest().body("添加角色权限失败");
        }
        log.info("添加角色权限成功，角色ID：{}，权限ID：{}", dto.getRoleId(), dto.getPermissionId());
        return ResponseEntity.ok("添加角色权限成功");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteRolePermission(@RequestBody RPDTO dto) {
        RolePermission byId = rolePermissionService.getRolePermissionById(dto.getRoleId(), dto.getPermissionId());
        if (byId == null) {
            return ResponseEntity.badRequest().body("该角色权限不存在");
        }
        boolean remove = rolePermissionService.removeById(byId.getId());
        if (!remove) {
            return ResponseEntity.badRequest().body("删除角色权限失败");
        }
        log.info("删除角色权限成功，角色ID：{}，权限ID：{}", dto.getRoleId(), dto.getPermissionId());
        return ResponseEntity.ok("删除角色权限成功");
    }

    @GetMapping("/permissionList/{roleId}")
    public ResponseEntity<?> getPermissionsByRoleId(@PathVariable int roleId) {
        List<RolePermission> rolePermissionList = rolePermissionService.getPermissionsByRoleId(roleId);
        if (rolePermissionList.isEmpty()) {
            return ResponseEntity.badRequest().body("该角色没有权限");
        }
        List<Permission> permissionList = null;
        try {
            permissionList = rolePermissionList.stream()
                    .map(rolePermission -> permissionService.getById(rolePermission.getPermissionId()))
                    .toList();
        } catch (Exception e) {
            log.error("获取权限列表错误：{}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok(permissionList);
    }

    @GetMapping("/roleList/{permissionId}")
    public ResponseEntity<?> getRolesByPermissionId(@PathVariable int permissionId) {
        List<RolePermission> rolePermissionList = rolePermissionService.getRolesByPermissionId(permissionId);
        if (rolePermissionList.isEmpty()) {
            return ResponseEntity.badRequest().body("该权限没有角色");
        }
        List<Role> roleList = null;
        try {
            roleList = rolePermissionList.stream()
                    .map(rolePermission -> roleService.getById(rolePermission.getRoleId()))
                    .toList();
        } catch (Exception e) {
            log.error("获取角色列表错误：{}", e.getMessage());
            return ResponseEntity.badRequest().body("获取角色列表错误：" + e.getMessage());
        }
        return ResponseEntity.ok(roleList);
    }
}

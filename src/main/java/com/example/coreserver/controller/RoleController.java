package com.example.coreserver.controller;


import com.example.coreserver.dto.RoleDTO;
import com.example.coreserver.entity.Role;
import com.example.coreserver.service.role.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
@RequestMapping("/api/role")
@Slf4j
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/allRoles")
    public ResponseEntity<?> getAllRoles() {
        List<Role> roleList = roleService.getAllRoles();
        return ResponseEntity.ok(roleList);
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody RoleDTO dto) {

        Role roleByName = roleService.getRoleByName(dto.getRoleName());
        Role roleByKey = roleService.getRoleByKey(dto.getRoleKey());
        if (roleByName != null || roleByKey != null) {
            return ResponseEntity.badRequest().body("角色名称或角色标识已存在");
        }

        Role role = new Role();
        role.setRoleName(dto.getRoleName());
        role.setRoleKey(dto.getRoleKey());
        role.setCreateTime(LocalDateTime.now());
        role.setStatus(0);
        role.setDeleted(false);
        boolean result = roleService.save(role);
        if (result) {
            log.info("添加角色成功: {}", role);
            return ResponseEntity.ok("添加成功");
        } else {
            return ResponseEntity.status(500).body("添加失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") String id) {
        Role role = roleService.getById(id);
        if (role == null) {
            return ResponseEntity.badRequest().body("角色不存在");
        }
        boolean result = roleService.removeById(role.getId());
        if (result) {
            log.info("删除角色成功: {}", role);
            return ResponseEntity.ok("删除成功");
        } else {
            return ResponseEntity.status(500).body("删除失败");
        }
    }


}

package com.example.coreserver.controller;


import com.example.coreserver.dto.URDTO;
import com.example.coreserver.entity.Role;
import com.example.coreserver.entity.User;
import com.example.coreserver.entity.UserRole;
import com.example.coreserver.service.role.RoleService;
import com.example.coreserver.service.role.UserRoleService;
import com.example.coreserver.service.role.UserService;
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
@RequestMapping("/api/userRole")
@Slf4j
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody URDTO dto) {
        UserRole userRoleById = userRoleService.getUserRoleById(dto.getUserId(), dto.getRoleId());
        if (userRoleById != null) {
            return ResponseEntity.badRequest().body("该用户角色已存在");
        }
        UserRole userRole = new UserRole();
        userRole.setUserId(dto.getUserId());
        userRole.setRoleId(dto.getRoleId());
        boolean save = userRoleService.save(userRole);
        if (!save) {
            return ResponseEntity.badRequest().body("添加用户角色失败");
        }
        log.info("添加用户角色成功，用户ID：{}，角色ID：{}", dto.getUserId(), dto.getRoleId());
        return ResponseEntity.ok("添加用户角色成功");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody URDTO dto) {
        UserRole userRoleById = userRoleService.getUserRoleById(dto.getUserId(), dto.getRoleId());
        if (userRoleById == null) {
            return ResponseEntity.badRequest().body("该用户角色不存在");
        }
        boolean remove = userRoleService.removeById(userRoleById);
        if (!remove) {
            return ResponseEntity.badRequest().body("删除用户角色失败");
        }
        log.info("删除用户角色成功，用户ID：{}，角色ID：{}", dto.getUserId(), dto.getRoleId());
        return ResponseEntity.ok("删除用户角色成功");
    }

    @GetMapping("/roleList/{userId}")
    public ResponseEntity<?> getRoleList(@PathVariable int userId) {
        List<UserRole> userRoles = userRoleService.getUserRolesByUserId(userId);
        if (userRoles == null || userRoles.isEmpty()) {
            return ResponseEntity.badRequest().body("该用户没有角色");
        }
        List<Role> list = null;
        try {
            list = userRoles.stream()
                    .map(map -> roleService.getById(map.getRoleId()))
                    .toList();
        } catch (Exception e) {
            log.error("获取用户角色列表失败，用户ID：{}，异常信息：{}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("获取用户角色列表失败" + e.getMessage());
        }
        log.info("获取用户角色列表成功，用户ID：{}", userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/userList/{roleId}")
    public ResponseEntity<?> getUserList(@PathVariable int roleId) {
        List<UserRole> userRoles = userRoleService.getUserRolesByRoleId(roleId);
        if (userRoles == null || userRoles.isEmpty()) {
            return ResponseEntity.badRequest().body("该角色没有用户");
        }
        List<User> list = null;
        try {
            list = userRoles.stream()
                    .map(map -> userService.getById(map.getUserId()))
                    .toList();
        } catch (Exception e) {
            log.error("获取角色用户列表失败，角色ID：{}，异常信息：{}", roleId, e.getMessage());
            return ResponseEntity.badRequest().body("获取角色用户列表失败" + e.getMessage());
        }
        log.info("获取角色用户列表成功，角色ID：{}", roleId);
        return ResponseEntity.ok(list);
    }
}

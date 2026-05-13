package com.example.coreserver.controller;


import com.example.coreserver.dto.PwdDTO;
import com.example.coreserver.dto.UserDTO;
import com.example.coreserver.entity.User;
import com.example.coreserver.service.role.UserService;
import com.example.coreserver.utils.BaseContext;
import com.example.coreserver.utils.RedisUtils;
import com.example.coreserver.vo.UserLoginVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.annotation.Validated;
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
@Slf4j
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtils redisUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     * @param userDTO 用户登录信息
     * @return  统一返回结果
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Validated @RequestBody UserDTO userDTO) {
        UserLoginVo userLoginVo = userService.Login(userDTO);

        return ResponseEntity.ok(userLoginVo);
    }

    /**
     * 退出登录
     * @return  统一返回结果
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader("Authorization");
        if (ObjectUtils.isEmpty(token)) { // header没有token
            token = request.getParameter("Authorization");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.info("用户ID：{}，退出登录", BaseContext.getCurrentId());
            // 清除上下文
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            // 清理redis
            redisUtil.delete("token:" + token);
            // 清理ThreadLocal
            BaseContext.removeCurrentId();

        }
        return ResponseEntity.ok().build();
    }


    // 需要 "USER_VIEW" 权限才能访问
    @PreAuthorize("hasAuthority('USER_VIEW')")
    @GetMapping("/list")
    public ResponseEntity<?> getList() {
        return ResponseEntity.ok(userService.list());
    }

    // 需要 "USER_ADD" 权限才能访问
    @PreAuthorize("hasAuthority('USER_ADD')")
    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody UserDTO userDTO) {
        User userByUsername = userService.getUserByUsername(userDTO.getUsername());
        if (userByUsername != null) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        user.setStatus(0);
        user.setDeleted(false);
        boolean res = userService.save(user);
        if (res) {
            return ResponseEntity.badRequest().body("添加失败");
        }
        log.info("添加用户成功，用户：{}", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<?> getUserInfo(@PathVariable int id) {
        User user = userService.getById(id);
        if (user == null) {
            return ResponseEntity.badRequest().body("用户不存在");
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody PwdDTO pwdDto) {
        User user = BaseContext.getCurrentId();
        if (passwordEncoder.matches(passwordEncoder.encode(pwdDto.getOldPassword()), user.getPassword())) {
            return ResponseEntity.badRequest().body("旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(pwdDto.getNewPassword()));
        boolean res = userService.updateById(user);
        if (res) {
            return ResponseEntity.badRequest().body("更新失败");
        }
        log.info("更新用户成功，用户：{}", user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        boolean res = userService.removeById(id);
        if (res) {
            return ResponseEntity.badRequest().body("删除失败");
        }
        log.info("删除用户成功，用户ID：{}", id);
        return ResponseEntity.ok("删除成功");
    }

}

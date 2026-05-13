package com.example.coreserver.filter;

import com.alibaba.fastjson2.JSON;
import com.example.coreserver.entity.UserLogin;
import com.example.coreserver.exception.CustomerAuthenticationException;
import com.example.coreserver.handler.LoginFailureHandler;
import com.example.coreserver.jwt.JwtClaimsConstant;
import com.example.coreserver.jwt.JwtProperties;
import com.example.coreserver.utils.BaseContext;
import com.example.coreserver.utils.JwtUtil;
import com.example.coreserver.utils.RedisUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @description 每次请求的 Security 过滤类。执行jwt有效性检查，如果失败，不会设置 SecurityContextHolder 信息，会进入 AuthenticationEntryPoint
 */
// 每一个servlet请求,只执行一次
@Component
@Slf4j
public class JwtTokenOncePerRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtProperties jwtProperties; // JWT相关属性配置类

    @Autowired
    private RedisUtils redisUtil; // Redis工具类

    @Autowired
    private LoginFailureHandler loginFailureHandler;

    // 添加白名单路径列表
    private final String[] whitelist = {
            "/api/user/login",
            //"/api/**",
            "/ws/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/doc.html"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 判断当前请求是否在白名单中
        String uri = request.getRequestURI();
        if (isWhitelisted(uri)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            // 2. 校验token
            this.validateToken(request);
        } catch (AuthenticationException e) {
            loginFailureHandler.onAuthenticationFailure(request, response, e); // 处理登录失败的异常
            return;
        }
        filterChain.doFilter(request, response);
    }

    // 判断请求路径是否在白名单中
    private boolean isWhitelisted(String uri) {
        for (String pattern : whitelist) {
            if (pattern.endsWith("/**")) {
                // 处理通配符路径
                String basePattern = pattern.substring(0, pattern.length() - 3);
                if (uri.startsWith(basePattern)) {
                    return true;
                }
            } else if (pattern.equals(uri)) {
                // 精确匹配
                return true;
            }
        }
        return false;
    }

    // 校验token
    private void validateToken(HttpServletRequest request) {
//        log.info("开始校验token，路径为：{}", request.getRequestURI());
        // 说明：登录了，再次请求其他需要认证的资源
        String token = request.getHeader("Authorization");

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (ObjectUtils.isEmpty(token)) { // header没有token
            token = request.getParameter("Authorization");
        }
        if (ObjectUtils.isEmpty(token)) {
            throw new CustomerAuthenticationException("token为空");
        }
        // redis进行校验
        if (!redisUtil.hasKey("token:" + token)) {
            throw new CustomerAuthenticationException("token已过期");
        }
        // 校验token
        UserLogin userLogin;
        try {
//            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            String loginUserString = claims.get(JwtClaimsConstant.USER_LOGIN).toString(); //JwtClaimsConstant.EMP_LOGIN就是一个常量字符串“empLogin”
            // 把json字符串转为对象
            userLogin = JSON.parseObject(loginUserString, UserLogin.class);
//            log.info("当前用户id：{}", userLogin.getUser().getId());
            BaseContext.setCurrentId(userLogin.getUser());
        } catch (Exception ex) {
            throw new CustomerAuthenticationException("token校验失败");
        }
        BaseContext.setCurrentId(userLogin.getUser());
        // 把校验后的用户信息再次放入到SpringSecurity的上下文中
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userLogin, null,userLogin.getAuthorities()); // 已认证的 Authentication 对象，包含用户的权限信息
        SecurityContextHolder.getContext().setAuthentication(authentication);
//        System.out.println(userLogin.getAuthorities());
    }
}


package com.example.coreserver.handler;

import com.alibaba.fastjson2.JSON;
import com.example.coreserver.common.RespCodeEnum;
import com.example.coreserver.common.Result;
import com.example.coreserver.exception.CustomerAuthenticationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @description 用户校验认证失败的处理器
 */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // 发生这个行为，做响应处理，给一个响应的结果
        response.setContentType("application/json;charset=utf-8");
        // 构建输出流对象
        ServletOutputStream outputStream = response.getOutputStream();
        String message;
        if (exception instanceof AccountExpiredException) {
            message = "用户过期，登录失败";
        } else if (exception instanceof BadCredentialsException) {
            message = "用户名或密码错误，请重新输入！";
        } else if (exception instanceof CredentialsExpiredException) {
            message = "密码过期，请重新输入！";
        } else if (exception instanceof DisabledException) {
            message = "账户被禁用，登录失败！";
        } else if (exception instanceof LockedException) {
            message = "账户被锁，登录失败！";
        } else if (exception instanceof InternalAuthenticationServiceException) {
            message = "账户不存在，登录失败！";
        } else if (exception instanceof CustomerAuthenticationException) {
            message = exception.getMessage();
        } else {
            message = "登录失败！";
        }
        // 调用fastjson工具，进行Result对象序列化
        String error = JSON.toJSONString(Result.error(RespCodeEnum.USER_UNAUTHENTICATED,message));
        outputStream.write(error.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
    }
}


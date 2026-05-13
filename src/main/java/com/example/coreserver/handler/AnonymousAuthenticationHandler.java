package com.example.coreserver.handler;

import com.alibaba.fastjson2.JSON;
import com.example.coreserver.common.RespCodeEnum;
import com.example.coreserver.common.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @description 客户端进行认证数据的提交时出现异常，或者是匿名用户访问受限资源的处理器
 */
@Component
public class AnonymousAuthenticationHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {


        // 发生这个行为，做响应处理，给一个响应的结果
        response.setContentType("application/json;charset=utf-8");
        // 构建输出流对象
        ServletOutputStream outputStream = response.getOutputStream();
        // 调用fastjson工具，进行Result对象序列化
        String error = "";
        if (authException instanceof BadCredentialsException){
            // 用户名或密码错误  401
            error = JSON.toJSONString(Result.error(RespCodeEnum.USER_UNAUTHENTICATED,authException.getMessage()));
        } else if (authException instanceof InternalAuthenticationServiceException) {
            error = JSON.toJSONString(Result.error(RespCodeEnum.USER_UNAUTHENTICATED,"用户名为空"));
        } else{
            error = JSON.toJSONString(Result.error(RespCodeEnum.FORBIDDEN,"匿名用户无权限访问"));
        }
        outputStream.write(error.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();

    }
}


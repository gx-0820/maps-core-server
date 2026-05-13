package com.example.coreserver.handler;

import com.alibaba.fastjson2.JSON;
import com.example.coreserver.common.RespCodeEnum;
import com.example.coreserver.common.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @description 认证用户无权限访问的处理器
 */
@Component
@Slf4j
public class CustomerAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.error("权限不足，URI：{}，异常：{}", request.getRequestURI(), accessDeniedException.getMessage());
        // 发生这个行为，做响应处理，给一个响应的结果
        response.setContentType("application/json;charset=utf-8");
        // 构建输出流对象
        ServletOutputStream outputStream = response.getOutputStream();
        // 调用fastjson工具，进行Result对象序列化
        String error = JSON.toJSONString(Result.error(RespCodeEnum.FORBIDDEN,"权限不足，请联系管理员"));
        outputStream.write(error.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
    }
}


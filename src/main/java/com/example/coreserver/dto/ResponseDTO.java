package com.example.coreserver.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 统一响应格式
 * @param <T> 数据类型
 */
@Data
public class ResponseDTO<T> {
    // 状态码（按业务规范定义）
    private int code;

    // 业务提示信息
    private String message;

    // 响应数据
    private T data;

    // 服务器时间戳
    private LocalDateTime timestamp = LocalDateTime.now();

    // 成功响应快捷方法
    public static <T> ResponseDTO<T> ok(T data) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.code = 200;
        response.message = "success";
        response.data = data;
        return response;
    }

    // 失败响应快捷方法（基础版）
    public static <T> ResponseDTO<T> fail(String message) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.code = 500;
        response.message = message;
        return response;
    }

    // 带状态码的失败响应
    public static <T> ResponseDTO<T> fail(int code, String message) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.code = code;
        response.message = message;
        return response;
    }

    // 带状态码和数据的响应
    public static <T> ResponseDTO<T> of(int code, String message, T data) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.code = code;
        response.message = message;
        response.data = data;
        return response;
    }
}
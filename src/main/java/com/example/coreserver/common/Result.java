package com.example.coreserver.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result {
    private long code;
    private String message;
    private String time;
    private long timestamp;
    private Object data;

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Result success() {
        return new Result(RespCodeEnum.SUCCESS.getCode(), RespCodeEnum.SUCCESS.getMessage(), LocalDateTime.now().format(formatter), System.currentTimeMillis(), null);
    }

    public static Result success(Object obj) {
        return new Result(RespCodeEnum.SUCCESS.getCode(), RespCodeEnum.SUCCESS.getMessage(), LocalDateTime.now().format(formatter), System.currentTimeMillis(), obj);
    }

    public static Result error(RespCodeEnum respCodeEnum) {
        return new Result(respCodeEnum.getCode(),respCodeEnum.getMessage(), LocalDateTime.now().format(formatter), System.currentTimeMillis(), null);
    }


    public static Result error(RespCodeEnum respCodeEnum, Object obj) {
        return new Result(respCodeEnum.getCode(),respCodeEnum.getMessage(), LocalDateTime.now().format(formatter), System.currentTimeMillis(), obj);
    }

    public static Result error(int code, String message) {
        return new Result(code, message, LocalDateTime.now().format(formatter), System.currentTimeMillis(), null);
    }
}

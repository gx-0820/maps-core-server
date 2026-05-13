package com.example.coreserver.common;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum RespCodeEnum {

    // ==================== 200 OK ====================
    SUCCESS(2000,"succer"),

    // ==================== 400 BadRequest ====================
    REQUEST_ERROR(4000,"request error"),

    CREATION_ERROR(4001,"creation error"),

    // ==================== 401 Unauthorized ====================
    ACCESS_TOKEN_EXPIRED_ERROR(4010,"access token expired error"),

    USER_UNAUTHENTICATED(4012,"user unauthenticated"),

    // ==================== 403 Forbidden ====================
    FORBIDDEN(4030,"forbidden"),

    NOT_ENOUGH_INFORMATION(4031,"not enough information"),

    PRE_CHECK_FAILED(4032,"pre check failed"),

    // ==================== 404 NotFound ====================
    NOT_FOUND(4040,"not found"),

    // ==================== 500 InternalServerError ====================
    SERVER_ERROR(5000,"server error");

    private final int code;
    private final String message;

    public HttpStatus getStatus() {

        int httpCode = code / 10;
        return HttpStatus.valueOf(httpCode);
    }

    public int getCode() {
        return code / 10;
    }

    public String getMessage() {
        return message;
    }
}

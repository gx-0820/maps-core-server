package com.example.coreserver.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @description 自定义认证验证异常
 */
public class CustomerAuthenticationException extends AuthenticationException {

    public CustomerAuthenticationException(String msg) {
        super(msg);
    }
}


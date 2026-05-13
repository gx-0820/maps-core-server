package com.example.coreserver.exception;

public class DeviceOperationException extends RuntimeException {
    public DeviceOperationException(String message) {
        super(message);
    }
}
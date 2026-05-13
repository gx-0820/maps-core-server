package com.example.coreserver.exception;

public class DeviceBusyException extends RuntimeException {
    public DeviceBusyException(String message) {
        super(message);
    }
}

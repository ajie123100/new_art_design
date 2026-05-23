package com.artdesign.common.exception;

import com.artdesign.common.constant.HttpStatus;

public class ServiceException extends RuntimeException {
    private final int code;

    public ServiceException(String message) {
        this(HttpStatus.ERROR, message);
    }

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

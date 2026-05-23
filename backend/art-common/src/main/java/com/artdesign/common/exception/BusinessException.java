package com.artdesign.common.exception;

import com.artdesign.common.constant.HttpStatus;

public class BusinessException extends RuntimeException {
    private final int code;
    private final Object[] args;

    public BusinessException(String message) {
        this(HttpStatus.ERROR, message, null);
    }

    public BusinessException(int code, String message) {
        this(code, message, null);
    }

    public BusinessException(String message, Object[] args) {
        this(HttpStatus.ERROR, message, args);
    }

    public BusinessException(int code, String message, Object[] args) {
        super(message);
        this.code = code;
        this.args = args;
    }

    public int getCode() {
        return code;
    }

    public Object[] getArgs() {
        return args;
    }
}

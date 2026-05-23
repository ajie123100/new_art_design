package com.artdesign.common.exception;

public class UserException extends BusinessException {
    public UserException(String message) {
        super(message);
    }

    public UserException(int code, String message) {
        super(code, message);
    }
}

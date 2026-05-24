package com.artdesign.common.constant;

public final class ErrorCode {
    public static final int SUCCESS = HttpStatus.SUCCESS;
    public static final int BAD_REQUEST = HttpStatus.ERROR;
    public static final int UNAUTHORIZED = HttpStatus.UNAUTHORIZED;
    public static final int FORBIDDEN = HttpStatus.FORBIDDEN;
    public static final int NOT_FOUND = HttpStatus.NOT_FOUND;
    public static final int CONFLICT = HttpStatus.CONFLICT;
    public static final int SERVER_ERROR = HttpStatus.SERVER_ERROR;

    public static final int AUTH_TOKEN_INVALID = 1001;
    public static final int AUTH_CAPTCHA_INVALID = 1002;
    public static final int AUTH_ACCOUNT_LOCKED = 1003;
    public static final int AUTH_RATE_LIMITED = 1004;

    public static final int USER_NOT_FOUND = 2001;
    public static final int USER_NAME_DUPLICATED = 2002;
    public static final int ROLE_NOT_FOUND = 2101;
    public static final int DEPT_HAS_CHILD = 2201;
    public static final int DEPT_HAS_USER = 2202;

    public static final int FILE_INVALID_TYPE = 3001;
    public static final int FILE_TOO_LARGE = 3002;
    public static final int FILE_NOT_FOUND = 3003;

    private ErrorCode() {
    }
}

package com.artdesign.common.constant;

public final class HttpStatus {
    public static final int SUCCESS = 200;
    public static final int CREATED = 201;
    public static final int ACCEPTED = 202;
    public static final int WARN = 300;
    public static final int ERROR = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int CONFLICT = 409;
    public static final int SERVER_ERROR = 500;

    private HttpStatus() {
    }
}

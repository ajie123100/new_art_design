package com.artdesign.common.core.domain;

import com.artdesign.common.constant.HttpStatus;

public class R<T> {
    public static final String CODE_TAG = "code";
    public static final String MSG_TAG = "msg";
    public static final String DATA_TAG = "data";

    private int code;
    private String msg;
    private T data;

    public R() {
    }

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(HttpStatus.SUCCESS, "success", null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(HttpStatus.SUCCESS, "success", data);
    }

    public static <T> R<T> ok(String msg) {
        return new R<>(HttpStatus.SUCCESS, msg, null);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(HttpStatus.SUCCESS, msg, data);
    }

    public static <T> R<T> warn(String msg) {
        return new R<>(HttpStatus.WARN, msg, null);
    }

    public static <T> R<T> warn(String msg, T data) {
        return new R<>(HttpStatus.WARN, msg, data);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(HttpStatus.ERROR, msg, null);
    }

    public static <T> R<T> fail(T data) {
        return new R<>(HttpStatus.ERROR, "error", data);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> fail(String msg, T data) {
        return new R<>(HttpStatus.ERROR, msg, data);
    }

    public static <T> R<T> fail(int code, String msg, T data) {
        return new R<>(code, msg, data);
    }

    public static <T> R<T> error() {
        return fail("error");
    }

    public static <T> R<T> error(String msg) {
        return fail(msg);
    }

    public static <T> R<T> error(int code, String msg) {
        return fail(code, msg);
    }

    public boolean isSuccess() {
        return HttpStatus.SUCCESS == code;
    }

    public boolean isError() {
        return !isSuccess();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

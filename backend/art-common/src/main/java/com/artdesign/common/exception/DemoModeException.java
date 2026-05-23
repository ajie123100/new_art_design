package com.artdesign.common.exception;

public class DemoModeException extends RuntimeException {
    public DemoModeException() {
        super("演示模式，不允许操作");
    }
}

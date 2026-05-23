package com.artdesign.common.enums;

public enum BusinessStatus {
    SUCCESS("1"),
    FAIL("0");

    private final String code;

    BusinessStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

package com.artdesign.common.enums;

public enum BusinessType {
    OTHER(0),
    INSERT(1),
    UPDATE(2),
    DELETE(3),
    GRANT(4),
    EXPORT(5),
    IMPORT(6),
    FORCE(7),
    CLEAN(8);

    private final int code;

    BusinessType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

package com.artdesign.common.core.page;

import java.util.List;

public class PageResult<T> {
    private List<T> records;
    private long current;
    private long size;
    private long total;
    private int code;
    private String msg;

    public PageResult() {
    }

    public PageResult(List<T> records, long current, long size, long total) {
        this.records = records;
        this.current = current;
        this.size = size;
        this.total = total;
        this.code = 200;
        this.msg = "success";
    }

    public static <T> PageResult<T> of(List<T> records, long current, long size, long total) {
        return new PageResult<>(records, current, size, total);
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getRows() {
        return records == null ? 0 : records.size();
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
}

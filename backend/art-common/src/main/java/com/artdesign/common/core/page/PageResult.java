package com.artdesign.common.core.page;

import java.util.List;

public class PageResult<T> {
    private List<T> records;
    private long pageNum;
    private long pageSize;
    private long total;
    private int code;
    private String msg;

    public PageResult() {
    }

    public PageResult(List<T> records, long pageNum, long pageSize, long total) {
        this.records = records;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.code = 200;
        this.msg = "success";
    }

    public static <T> PageResult<T> of(List<T> records, long pageNum, long pageSize, long total) {
        return new PageResult<>(records, pageNum, pageSize, total);
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
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

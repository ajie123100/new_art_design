package com.artdesign.common.core.page;

import com.artdesign.common.constant.HttpStatus;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class TableDataInfo<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private long total;
    private List<T> rows;
    private int code;
    private String msg;

    public TableDataInfo() {
    }

    public TableDataInfo(List<T> rows, long total) {
        this.rows = rows;
        this.total = total;
        this.code = HttpStatus.SUCCESS;
        this.msg = "success";
    }

    public static <T> TableDataInfo<T> of(List<T> rows, long total) {
        return new TableDataInfo<>(rows, total);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
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

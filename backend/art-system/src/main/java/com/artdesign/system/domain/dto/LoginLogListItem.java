package com.artdesign.system.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public record LoginLogListItem(
        @ExcelProperty("日志ID")
        Long infoId,
        @ExcelProperty("用户账号")
        String userName,
        @ExcelProperty("登录IP")
        String ipaddr,
        @ExcelProperty("浏览器")
        String browser,
        @ExcelProperty("操作系统")
        String os,
        @ExcelProperty("状态")
        String status,
        @ExcelProperty("消息")
        String msg,
        @ExcelProperty("登录时间")
        String loginTime
) {
}

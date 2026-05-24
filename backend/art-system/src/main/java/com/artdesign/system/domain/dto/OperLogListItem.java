package com.artdesign.system.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public record OperLogListItem(
        @ExcelProperty("日志ID")
        Long operId,
        @ExcelProperty("模块标题")
        String title,
        @ExcelProperty("业务类型")
        Integer businessType,
        @ExcelProperty("操作人员")
        String operName,
        @ExcelProperty("操作IP")
        String operIp,
        @ExcelProperty("请求地址")
        String operUrl,
        @ExcelProperty("请求参数")
        String operParam,
        @ExcelProperty("状态")
        String status,
        @ExcelProperty("错误消息")
        String errorMsg,
        @ExcelProperty("耗时毫秒")
        Long costTime,
        @ExcelProperty("操作时间")
        String operTime
) {
}

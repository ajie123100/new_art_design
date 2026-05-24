package com.artdesign.system.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public record JobLogListItem(
        @ExcelProperty("日志ID")
        Long jobLogId,
        @ExcelProperty("任务名称")
        String jobName,
        @ExcelProperty("任务组名")
        String jobGroup,
        @ExcelProperty("调用目标")
        String invokeTarget,
        @ExcelProperty("日志信息")
        String jobMessage,
        @ExcelProperty("状态")
        String status,
        @ExcelProperty("耗时毫秒")
        Long elapsedMs,
        @ExcelProperty("执行时间")
        String createTime
) {
}

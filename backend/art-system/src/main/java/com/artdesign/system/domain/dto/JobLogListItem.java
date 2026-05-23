package com.artdesign.system.domain.dto;

public record JobLogListItem(
        Long jobLogId,
        String jobName,
        String jobGroup,
        String invokeTarget,
        String jobMessage,
        String status,
        String createTime
) {
}

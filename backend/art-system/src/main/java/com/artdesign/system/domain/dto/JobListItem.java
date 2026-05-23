package com.artdesign.system.domain.dto;

public record JobListItem(
        Long jobId,
        String jobName,
        String jobGroup,
        String invokeTarget,
        String cronExpression,
        String status,
        String createTime
) {
}

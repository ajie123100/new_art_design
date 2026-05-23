package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record JobSaveRequest(
        Long jobId,
        @NotBlank String jobName,
        String jobGroup,
        @NotBlank String invokeTarget,
        @NotBlank String cronExpression,
        String misfirePolicy,
        String concurrent,
        String status,
        String remark
) {
}

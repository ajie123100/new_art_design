package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotNull;

public record DeptStatusRequest(
        @NotNull Long deptId,
        @NotNull Boolean enabled
) {
}

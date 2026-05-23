package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeptSaveRequest(
        Long deptId,
        Long parentId,
        @NotBlank String deptName,
        @NotNull Integer orderNum,
        String leader,
        String phone,
        String email,
        Boolean enabled
) {
}

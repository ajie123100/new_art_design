package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record RoleSaveRequest(
        Long roleId,
        @NotBlank String roleName,
        @NotBlank String roleCode,
        String description,
        String dataScope,
        List<Long> deptIds,
        Boolean enabled
) {
}

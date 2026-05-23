package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleSaveRequest(
        Long roleId,
        @NotBlank String roleName,
        @NotBlank String roleCode,
        String description,
        Boolean enabled
) {
}

package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotNull;

public record RoleStatusRequest(
        @NotNull Long roleId,
        @NotNull Boolean enabled
) {
}

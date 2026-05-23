package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RoleMenuRequest(
        @NotNull Long roleId,
        List<Long> menuIds
) {
}

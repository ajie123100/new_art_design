package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotNull;

public record MenuStatusRequest(
        @NotNull Long menuId,
        @NotNull Boolean enabled
) {
}

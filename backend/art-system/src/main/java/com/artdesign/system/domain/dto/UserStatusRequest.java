package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(
        @NotNull Long id,
        @NotNull String status
) {
}

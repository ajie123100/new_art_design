package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserResetPasswordRequest(
        @NotNull Long id,
        @NotBlank String password
) {
}

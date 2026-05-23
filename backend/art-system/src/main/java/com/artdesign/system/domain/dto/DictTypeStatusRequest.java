package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotNull;

public record DictTypeStatusRequest(
        @NotNull Long dictId,
        @NotNull Boolean enabled
) {
}

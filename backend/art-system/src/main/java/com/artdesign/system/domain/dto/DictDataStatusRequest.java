package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotNull;

public record DictDataStatusRequest(
        @NotNull Long dictCode,
        @NotNull Boolean enabled
) {
}

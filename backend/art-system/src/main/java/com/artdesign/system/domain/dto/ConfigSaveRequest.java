package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfigSaveRequest(
        Long configId,
        @NotBlank String configName,
        @NotBlank String configKey,
        @NotBlank String configValue,
        String configType,
        String remark
) {
}

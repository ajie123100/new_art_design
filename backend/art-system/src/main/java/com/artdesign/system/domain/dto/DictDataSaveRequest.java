package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DictDataSaveRequest(
        Long dictCode,
        @NotNull Integer dictSort,
        @NotBlank String dictLabel,
        @NotBlank String dictValue,
        @NotBlank String dictType,
        String cssClass,
        String listClass,
        Boolean defaultValue,
        Boolean enabled,
        String remark
) {
}

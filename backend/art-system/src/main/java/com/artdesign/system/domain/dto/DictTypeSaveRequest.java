package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record DictTypeSaveRequest(
        Long dictId,
        @NotBlank String dictName,
        @NotBlank String dictType,
        Boolean enabled,
        String remark
) {
}

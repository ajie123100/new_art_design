package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostSaveRequest(
        Long postId,
        @NotBlank String postCode,
        @NotBlank String postName,
        Integer postSort,
        Boolean enabled,
        String remark
) {
}

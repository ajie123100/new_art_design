package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record NoticeSaveRequest(
        Long noticeId,
        @NotBlank String noticeTitle,
        @NotBlank String noticeType,
        String noticeContent,
        String status,
        String remark
) {
}

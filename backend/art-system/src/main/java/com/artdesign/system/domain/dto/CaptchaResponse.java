package com.artdesign.system.domain.dto;

public record CaptchaResponse(
        Boolean captchaEnabled,
        String uuid,
        String img
) {
}

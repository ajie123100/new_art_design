package com.artdesign.system.domain.dto;

public record LoginResponse(String token, String refreshToken, Long expiresIn) {
}

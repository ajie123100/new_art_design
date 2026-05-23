package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UserSaveRequest(
        Long id,
        @NotBlank String userName,
        String nickName,
        String userPhone,
        String userEmail,
        String userGender,
        String status,
        String password,
        List<String> userRoles
) {
}

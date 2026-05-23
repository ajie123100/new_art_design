package com.artdesign.system.domain.dto;

import java.util.List;

public record UserInfo(
        List<String> buttons,
        List<String> roles,
        Long userId,
        String userName,
        String email,
        String avatar
) {
}

package com.artdesign.system.domain.dto;

import java.util.List;

public record UserListItem(
        Long id,
        String avatar,
        String status,
        String userName,
        String userGender,
        String nickName,
        String userPhone,
        String userEmail,
        List<String> userRoles,
        String createBy,
        String createTime,
        String updateBy,
        String updateTime
) {
}

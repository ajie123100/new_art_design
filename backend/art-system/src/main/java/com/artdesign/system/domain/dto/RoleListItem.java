package com.artdesign.system.domain.dto;

public record RoleListItem(
        Long roleId,
        String roleName,
        String roleCode,
        String description,
        boolean enabled,
        String createTime
) {
}

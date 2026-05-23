package com.artdesign.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MenuSaveRequest(
        Long menuId,
        Long parentId,
        @NotBlank String menuName,
        @NotNull Integer orderNum,
        String path,
        String component,
        String routeName,
        Boolean external,
        Boolean keepAlive,
        @NotBlank String menuType,
        Boolean visible,
        Boolean enabled,
        String perms,
        String icon,
        String remark
) {
}

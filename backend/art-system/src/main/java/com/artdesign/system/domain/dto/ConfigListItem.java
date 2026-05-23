package com.artdesign.system.domain.dto;

public record ConfigListItem(
        Long configId,
        String configName,
        String configKey,
        String configValue,
        String configType,
        String createTime
) {
}

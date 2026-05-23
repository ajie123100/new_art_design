package com.artdesign.system.domain.dto;

import java.util.Map;

public record CacheInfo(
        String commandStats,
        String dbSize,
        String info,
        String keys,
        Map<String, Object> properties
) {
}

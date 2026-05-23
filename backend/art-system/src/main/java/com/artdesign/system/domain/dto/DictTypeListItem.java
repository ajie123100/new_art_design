package com.artdesign.system.domain.dto;

public record DictTypeListItem(
        Long dictId,
        String dictName,
        String dictType,
        boolean enabled,
        String remark,
        String createTime,
        String updateTime
) {
}

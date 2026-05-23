package com.artdesign.system.domain.dto;

public record DictDataListItem(
        Long dictCode,
        Integer dictSort,
        String dictLabel,
        String dictValue,
        String dictType,
        String cssClass,
        String listClass,
        boolean defaultValue,
        boolean enabled,
        String remark,
        String createTime,
        String updateTime
) {
}

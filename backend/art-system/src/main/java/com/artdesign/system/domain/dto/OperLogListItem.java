package com.artdesign.system.domain.dto;

public record OperLogListItem(
        Long operId,
        String title,
        Integer businessType,
        String operName,
        String operIp,
        String operUrl,
        String operParam,
        String status,
        String errorMsg,
        Long costTime,
        String operTime
) {
}

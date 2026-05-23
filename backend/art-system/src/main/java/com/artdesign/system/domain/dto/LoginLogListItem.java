package com.artdesign.system.domain.dto;

public record LoginLogListItem(
        Long infoId,
        String userName,
        String ipaddr,
        String browser,
        String os,
        String status,
        String msg,
        String loginTime
) {
}

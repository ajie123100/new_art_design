package com.artdesign.system.domain.dto;

public record OnlineUser(
        Long tokenId,
        String loginName,
        String deptName,
        String ipaddr,
        String loginLocation,
        String browser,
        String os,
        String loginTime
) {
}

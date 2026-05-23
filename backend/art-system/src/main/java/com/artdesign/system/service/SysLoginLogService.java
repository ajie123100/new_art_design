package com.artdesign.system.service;

import com.artdesign.common.enums.BusinessStatus;
import com.artdesign.system.domain.entity.SysLoginLog;
import com.artdesign.system.mapper.SysLoginLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SysLoginLogService {
    private final SysLoginLogMapper loginLogMapper;

    public SysLoginLogService(SysLoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
    }

    public void record(String userName, String ipaddr, String userAgent, BusinessStatus status, String message) {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUserName(defaultString(userName));
        loginLog.setIpaddr(defaultString(ipaddr));
        loginLog.setLoginLocation("内网IP");
        loginLog.setBrowser(parseBrowser(userAgent));
        loginLog.setOs(parseOs(userAgent));
        loginLog.setStatus(status.getCode());
        loginLog.setMsg(defaultString(message));
        loginLog.setLoginTime(LocalDateTime.now());
        loginLogMapper.insert(loginLog);
    }

    private String parseBrowser(String userAgent) {
        if (!hasText(userAgent)) {
            return "";
        }
        if (userAgent.contains("Edg/")) {
            return "Edge";
        }
        if (userAgent.contains("Chrome/")) {
            return "Chrome";
        }
        if (userAgent.contains("Firefox/")) {
            return "Firefox";
        }
        if (userAgent.contains("Safari/")) {
            return "Safari";
        }
        return "Unknown";
    }

    private String parseOs(String userAgent) {
        if (!hasText(userAgent)) {
            return "";
        }
        if (userAgent.contains("Windows")) {
            return "Windows";
        }
        if (userAgent.contains("Mac OS")) {
            return "Mac OS";
        }
        if (userAgent.contains("Linux")) {
            return "Linux";
        }
        if (userAgent.contains("Android")) {
            return "Android";
        }
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "iOS";
        }
        return "Unknown";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}

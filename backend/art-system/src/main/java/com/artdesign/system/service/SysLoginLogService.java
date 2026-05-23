package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.enums.BusinessStatus;
import com.artdesign.system.domain.dto.LoginLogListItem;
import com.artdesign.system.domain.entity.SysLoginLog;
import com.artdesign.system.mapper.SysLoginLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class SysLoginLogService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    public PageResult<LoginLogListItem> list(Map<String, String> params) {
        long current = parseLong(params.get("current"), 1L);
        long size = parseLong(params.get("size"), 10L);

        List<SysLoginLog> logs = loginLogMapper.selectList(new LambdaQueryWrapper<SysLoginLog>()
                .like(hasText(params.get("userName")), SysLoginLog::getUserName, params.get("userName"))
                .like(hasText(params.get("ipaddr")), SysLoginLog::getIpaddr, params.get("ipaddr"))
                .eq(hasText(params.get("status")), SysLoginLog::getStatus, params.get("status"))
                .ge(hasText(params.get("beginTime")), SysLoginLog::getLoginTime, params.get("beginTime"))
                .le(hasText(params.get("endTime")), SysLoginLog::getLoginTime, params.get("endTime"))
                .orderByDesc(SysLoginLog::getLoginTime));

        List<LoginLogListItem> records = logs.stream()
                .map(this::toLoginLogListItem)
                .toList();
        return page(records, current, size);
    }

    private LoginLogListItem toLoginLogListItem(SysLoginLog log) {
        return new LoginLogListItem(
                log.getInfoId(),
                log.getUserName(),
                log.getIpaddr(),
                log.getBrowser(),
                log.getOs(),
                log.getStatus(),
                log.getMsg(),
                formatDateTime(log.getLoginTime())
        );
    }

    private <T> PageResult<T> page(List<T> records, long current, long size) {
        int from = (int) Math.min(Math.max(current - 1, 0) * size, records.size());
        int to = (int) Math.min(from + size, records.size());
        return PageResult.of(records.subList(from, to), current, size, records.size());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    private long parseLong(String value, long fallback) {
        if (!hasText(value)) {
            return fallback;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
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

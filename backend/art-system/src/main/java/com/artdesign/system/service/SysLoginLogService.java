package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.core.page.PageUtils;
import com.artdesign.common.enums.BusinessStatus;
import com.artdesign.system.domain.dto.LoginLogListItem;
import com.artdesign.system.domain.entity.SysLoginLog;
import com.artdesign.system.mapper.SysLoginLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        long current = PageUtils.pageNum(params);
        long size = PageUtils.pageSize(params);

        Page<SysLoginLog> page = new Page<>(current, size);
        IPage<SysLoginLog> result = loginLogMapper.selectPage(page, buildQuery(params));

        List<LoginLogListItem> records = result.getRecords().stream()
                .map(this::toLoginLogListItem)
                .toList();
        return new PageResult<>(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    public List<LoginLogListItem> exportList(Map<String, String> params) {
        return loginLogMapper.selectList(buildQuery(params)).stream().map(this::toLoginLogListItem).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void clean() {
        loginLogMapper.delete(new LambdaQueryWrapper<>());
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

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    private LambdaQueryWrapper<SysLoginLog> buildQuery(Map<String, String> params) {
        Map<String, String> queryParams = params == null ? Map.of() : params;
        return new LambdaQueryWrapper<SysLoginLog>()
                .like(hasText(queryParams.get("userName")), SysLoginLog::getUserName, queryParams.get("userName"))
                .like(hasText(queryParams.get("ipaddr")), SysLoginLog::getIpaddr, queryParams.get("ipaddr"))
                .eq(hasText(queryParams.get("status")), SysLoginLog::getStatus, queryParams.get("status"))
                .ge(hasText(queryParams.get("beginTime")), SysLoginLog::getLoginTime, queryParams.get("beginTime"))
                .le(hasText(queryParams.get("endTime")), SysLoginLog::getLoginTime, queryParams.get("endTime"))
                .orderByDesc(SysLoginLog::getLoginTime);
    }

    private long parseLong(String value, long fallback) {
        if (!hasText(value)) return fallback;
        try { return Long.parseLong(value); } catch (NumberFormatException ignored) { return fallback; }
    }

    private String parseBrowser(String userAgent) {
        if (!hasText(userAgent)) return "";
        if (userAgent.contains("Edg/")) return "Edge";
        if (userAgent.contains("Chrome/")) return "Chrome";
        if (userAgent.contains("Firefox/")) return "Firefox";
        if (userAgent.contains("Safari/")) return "Safari";
        return "Unknown";
    }

    private String parseOs(String userAgent) {
        if (!hasText(userAgent)) return "";
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac OS")) return "Mac OS";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        return "Unknown";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}

package com.artdesign.system.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.artdesign.system.domain.dto.OnlineUser;
import com.artdesign.system.domain.entity.SysDept;
import com.artdesign.system.domain.entity.SysUser;
import com.artdesign.system.mapper.SysDeptMapper;
import com.artdesign.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class SysOnlineUserService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;

    public SysOnlineUserService(SysUserMapper userMapper, SysDeptMapper deptMapper) {
        this.userMapper = userMapper;
        this.deptMapper = deptMapper;
    }

    public List<OnlineUser> list() {
        return StpUtil.searchTokenValue("", 0, -1, false).stream()
                .map(this::toOnlineUser)
                .filter(Objects::nonNull)
                .toList();
    }

    public void forceLogout(String tokenValue) {
        StpUtil.logoutByTokenValue(tokenValue);
    }

    private OnlineUser toOnlineUser(String tokenValue) {
        try {
            Object loginId = StpUtil.getLoginIdByToken(tokenValue);
            if (loginId == null) {
                return null;
            }
            SysUser user = userMapper.selectById(Long.valueOf(loginId.toString()));
            String loginName = user == null ? loginId.toString() : user.getUserName();
            String deptName = deptName(user);
            var tokenSession = StpUtil.getTokenSessionByToken(tokenValue);
            return new OnlineUser(
                    tokenValue,
                    loginName,
                    deptName,
                    getString(tokenSession.get("ipaddr")),
                    getString(tokenSession.get("loginLocation")),
                    getString(tokenSession.get("browser")),
                    getString(tokenSession.get("os")),
                    formatLoginTime(tokenSession.get("loginTime"), tokenSession.getCreateTime()),
                    getString(tokenSession.get("deviceId"))
            );
        } catch (NotLoginException | NumberFormatException ignored) {
            return null;
        }
    }

    private String deptName(SysUser user) {
        if (user == null || user.getDeptId() == null) {
            return "";
        }
        SysDept dept = deptMapper.selectById(user.getDeptId());
        return dept == null ? "" : dept.getDeptName();
    }

    private String formatLoginTime(Object loginTime, long fallbackMillis) {
        if (loginTime instanceof LocalDateTime dateTime) {
            return FMT.format(dateTime);
        }
        if (loginTime instanceof String value && !value.isBlank()) {
            return value;
        }
        return FMT.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(fallbackMillis), ZONE_ID));
    }

    private String getString(Object value) {
        return value == null ? "" : value.toString();
    }
}

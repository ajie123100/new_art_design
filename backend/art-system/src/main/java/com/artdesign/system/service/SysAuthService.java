package com.artdesign.system.service;

import com.artdesign.common.exception.BusinessException;
import com.artdesign.common.enums.BusinessStatus;
import com.artdesign.system.domain.dto.UserInfo;
import com.artdesign.system.domain.entity.SysUser;
import com.artdesign.system.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class SysAuthService {
    private static final String SUPER_ROLE = "R_SUPER";
    private static final String SUPER_PERMISSION = "*:*:*";
    private static final String DEFAULT_AVATAR = "https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png";

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SysLoginLogService loginLogService;

    @Autowired
    public SysAuthService(SysUserMapper userMapper, PasswordEncoder passwordEncoder, SysLoginLogService loginLogService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.loginLogService = loginLogService;
    }

    public SysAuthService(SysUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this(userMapper, passwordEncoder, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public SysUser login(String userName, String password, String ipaddr, String userAgent) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, userName)
                .eq(SysUser::getDelFlag, "0")
                .last("LIMIT 1"));

        if (user == null || !passwordMatches(password, user.getPassword())) {
            recordLogin(userName, ipaddr, userAgent, BusinessStatus.FAIL, "用户名或密码错误");
            throw new BusinessException("用户名或密码错误");
        }
        if (!Objects.equals(user.getStatus(), "1")) {
            recordLogin(userName, ipaddr, userAgent, BusinessStatus.FAIL, "账号已停用");
            throw new BusinessException("账号已停用");
        }
        upgradePlainPasswordIfNeeded(user, password);
        updateLoginInfo(user, ipaddr);
        recordLogin(userName, ipaddr, userAgent, BusinessStatus.SUCCESS, "登录成功");
        return user;
    }

    public SysUser login(String userName, String password) {
        return login(userName, password, "", "");
    }

    public UserInfo getUserInfo(Long userId) {
        SysUser user = findUser(userId);
        List<String> roles = getRoleCodes(userId);
        List<String> buttons = getButtonList(userId);

        return new UserInfo(
                buttons,
                roles,
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                defaultIfBlank(user.getAvatar(), DEFAULT_AVATAR)
        );
    }

    public List<String> getRoleList(Long userId) {
        return getRoleCodes(userId);
    }

    public List<String> getPermissionList(Long userId) {
        List<String> roles = getRoleCodes(userId);
        if (roles.contains(SUPER_ROLE)) {
            return List.of(SUPER_PERMISSION);
        }
        return userMapper.selectPermissionsByUserId(userId);
    }

    private List<String> getButtonList(Long userId) {
        List<String> roles = getRoleCodes(userId);
        if (roles.contains(SUPER_ROLE)) {
            return List.of(SUPER_PERMISSION);
        }
        return userMapper.selectButtonsByUserId(userId);
    }

    private SysUser findUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || !Objects.equals(user.getDelFlag(), "0")) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private List<String> getRoleCodes(Long userId) {
        return userMapper.selectRoleCodesByUserId(userId);
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (!hasText(storedPassword)) {
            return false;
        }
        if (isBcrypt(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return Objects.equals(rawPassword, storedPassword);
    }

    private void upgradePlainPasswordIfNeeded(SysUser user, String rawPassword) {
        if (isBcrypt(user.getPassword())) {
            return;
        }
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setUpdateBy("system");
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private void updateLoginInfo(SysUser user, String ipaddr) {
        user.setLoginIp(defaultIfBlank(ipaddr, ""));
        user.setLoginDate(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private void recordLogin(String userName, String ipaddr, String userAgent, BusinessStatus status, String message) {
        if (loginLogService != null) {
            loginLogService.record(userName, ipaddr, userAgent, status, message);
        }
    }

    private boolean isBcrypt(String password) {
        return password != null && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }
}

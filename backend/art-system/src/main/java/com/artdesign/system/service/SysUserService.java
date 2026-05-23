package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.UserListItem;
import com.artdesign.system.domain.dto.UserResetPasswordRequest;
import com.artdesign.system.domain.dto.UserSaveRequest;
import com.artdesign.system.domain.dto.UserStatusRequest;
import com.artdesign.system.domain.entity.SysUser;
import com.artdesign.system.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class SysUserService {
    private static final String DEFAULT_AVATAR = "https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public SysUserService(SysUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<UserListItem> listUsers(Map<String, String> params) {
        long current = parseLong(params.get("current"), 1L);
        long size = parseLong(params.get("size"), 10L);

        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .like(hasText(params.get("userName")), SysUser::getUserName, params.get("userName"))
                .like(hasText(params.get("userEmail")), SysUser::getEmail, params.get("userEmail"))
                .like(hasText(params.get("userPhone")), SysUser::getPhonenumber, params.get("userPhone"))
                .eq(hasText(params.get("status")), SysUser::getStatus, params.get("status"))
                .eq(SysUser::getDelFlag, "0")
                .orderByAsc(SysUser::getUserId));

        List<UserListItem> records = users.stream()
                .map(this::toUserListItem)
                .toList();
        return page(records, current, size);
    }

    public UserListItem getUser(Long userId) {
        return toUserListItem(findUser(userId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserSaveRequest request) {
        ensureUniqueUserName(request.userName(), null);

        SysUser user = new SysUser();
        fillUser(user, request);
        user.setPassword(passwordEncoder.encode(defaultIfBlank(request.password(), "123456")));
        user.setStatus(defaultIfBlank(request.status(), "1"));
        user.setDelFlag("0");
        user.setCreateBy("system");
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        saveUserRoles(user.getUserId(), request.userRoles());
        return user.getUserId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserSaveRequest request) {
        if (request.id() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        SysUser user = findUser(request.id());
        ensureUniqueUserName(request.userName(), request.id());
        fillUser(user, request);
        if (hasText(request.password())) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        user.setUpdateBy("system");
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        saveUserRoles(user.getUserId(), request.userRoles());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("请选择要删除的用户");
        }
        for (Long userId : userIds) {
            if (Objects.equals(userId, 1L)) {
                throw new BusinessException("不能删除超级管理员");
            }
            SysUser user = findUser(userId);
            user.setDelFlag("1");
            user.setUpdateBy("system");
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            userMapper.deleteUserRoles(userId);
        }
    }

    public void updateStatus(UserStatusRequest request) {
        SysUser user = findUser(request.id());
        user.setStatus(request.status());
        user.setUpdateBy("system");
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    public void resetPassword(UserResetPasswordRequest request) {
        SysUser user = findUser(request.id());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setUpdateBy("system");
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    SysUser findUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || !Objects.equals(user.getDelFlag(), "0")) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private UserListItem toUserListItem(SysUser user) {
        return new UserListItem(
                user.getUserId(),
                defaultIfBlank(user.getAvatar(), DEFAULT_AVATAR),
                user.getStatus(),
                user.getUserName(),
                sexLabel(user.getSex()),
                user.getNickName(),
                user.getPhonenumber(),
                user.getEmail(),
                userMapper.selectRoleCodesByUserId(user.getUserId()),
                defaultIfBlank(user.getCreateBy(), "system"),
                formatDateTime(user.getCreateTime()),
                defaultIfBlank(user.getUpdateBy(), "system"),
                formatDateTime(user.getUpdateTime())
        );
    }

    private void fillUser(SysUser user, UserSaveRequest request) {
        user.setUserName(request.userName());
        user.setNickName(defaultIfBlank(request.nickName(), request.userName()));
        user.setPhonenumber(defaultIfBlank(request.userPhone(), ""));
        user.setEmail(defaultIfBlank(request.userEmail(), ""));
        user.setSex(normalizeSex(request.userGender()));
        user.setStatus(defaultIfBlank(request.status(), user.getStatus()));
    }

    private void ensureUniqueUserName(String userName, Long ignoredUserId) {
        SysUser existing = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, userName)
                .eq(SysUser::getDelFlag, "0")
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getUserId(), ignoredUserId)) {
            throw new BusinessException("用户名已存在");
        }
    }

    private void saveUserRoles(Long userId, List<String> roleCodes) {
        userMapper.deleteUserRoles(userId);
        if (roleCodes == null || roleCodes.isEmpty()) {
            return;
        }
        List<Long> roleIds = userMapper.selectRoleIdsByRoleCodes(roleCodes);
        for (Long roleId : roleIds) {
            userMapper.insertUserRole(userId, roleId);
        }
    }

    private <T> PageResult<T> page(List<T> records, long current, long size) {
        int from = (int) Math.min(Math.max(current - 1, 0) * size, records.size());
        int to = (int) Math.min(from + size, records.size());
        return PageResult.of(records.subList(from, to), current, size, records.size());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    private String sexLabel(String sex) {
        if (Objects.equals(sex, "0")) {
            return "男";
        }
        if (Objects.equals(sex, "1")) {
            return "女";
        }
        return "未知";
    }

    private String normalizeSex(String gender) {
        if (Objects.equals(gender, "女") || Objects.equals(gender, "1") || Objects.equals(gender, "2")) {
            return "1";
        }
        return "0";
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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }
}

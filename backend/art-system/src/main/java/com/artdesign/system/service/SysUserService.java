package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.core.page.PageUtils;
import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.ImportResult;
import com.artdesign.system.domain.dto.UserExcel;
import com.artdesign.system.domain.dto.UserListItem;
import com.artdesign.system.domain.dto.UserResetPasswordRequest;
import com.artdesign.system.domain.dto.UserSaveRequest;
import com.artdesign.system.domain.dto.UserStatusRequest;
import com.artdesign.system.domain.entity.SysUser;
import com.artdesign.system.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class SysUserService {
    private static final String DEFAULT_AVATAR = "https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern ROLE_SPLITTER = Pattern.compile("[,，\\s]+");

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final DataScopeService dataScopeService;
    private final SysLoginSecurityService loginSecurityService;

    public SysUserService(SysUserMapper userMapper, PasswordEncoder passwordEncoder, DataScopeService dataScopeService, SysLoginSecurityService loginSecurityService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.dataScopeService = dataScopeService;
        this.loginSecurityService = loginSecurityService;
    }

    public PageResult<UserListItem> listUsers(Map<String, String> params) {
        long pageNum = PageUtils.pageNum(params);
        long pageSize = PageUtils.pageSize(params);
        DataScopeService.DataScope dataScope = dataScopeService.currentDataScope();

        Page<SysUser> page = new Page<>(pageNum, pageSize);
        IPage<SysUser> result = userMapper.selectPage(page, new LambdaQueryWrapper<SysUser>()
                .like(hasText(params.get("userName")), SysUser::getUserName, params.get("userName"))
                .like(hasText(params.get("userEmail")), SysUser::getEmail, params.get("userEmail"))
                .like(hasText(params.get("userPhone")), SysUser::getPhonenumber, params.get("userPhone"))
                .eq(hasText(params.get("status")), SysUser::getStatus, params.get("status"))
                .in(dataScope.hasDeptLimit(), SysUser::getDeptId, dataScope.deptIds())
                .eq(dataScope.selfOnly(), SysUser::getUserId, dataScope.userId())
                .eq(SysUser::getDelFlag, "0")
                .orderByAsc(SysUser::getUserId));

        List<UserListItem> records = result.getRecords().stream()
                .map(this::toUserListItem)
                .toList();
        return new PageResult<>(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    public UserListItem getUser(Long userId) {
        return toUserListItem(findUser(userId));
    }

    public List<UserExcel> exportUsers(Map<String, String> params) {
        DataScopeService.DataScope dataScope = dataScopeService.currentDataScope();
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .like(hasText(params.get("userName")), SysUser::getUserName, params.get("userName"))
                        .like(hasText(params.get("userEmail")), SysUser::getEmail, params.get("userEmail"))
                        .like(hasText(params.get("userPhone")), SysUser::getPhonenumber, params.get("userPhone"))
                        .eq(hasText(params.get("status")), SysUser::getStatus, params.get("status"))
                        .in(dataScope.hasDeptLimit(), SysUser::getDeptId, dataScope.deptIds())
                        .eq(dataScope.selfOnly(), SysUser::getUserId, dataScope.userId())
                        .eq(SysUser::getDelFlag, "0")
                        .orderByAsc(SysUser::getUserId))
                .stream()
                .map(this::toUserExcel)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public ImportResult importUsers(List<UserExcel> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new BusinessException("导入用户不能为空");
        }
        int count = 0;
        int skipped = 0;
        for (UserExcel row : rows) {
            if (row == null || !hasText(row.userName)) {
                skipped++;
                continue;
            }
            SysUser existing = findImportUser(row);
            UserSaveRequest request = new UserSaveRequest(
                    existing == null ? row.userId : existing.getUserId(),
                    row.userName,
                    row.nickName,
                    row.userPhone,
                    row.userEmail,
                    row.userGender,
                    row.status,
                    row.password,
                    parseRoles(row.userRoles)
            );
            if (existing == null) {
                createUser(request);
            } else {
                updateUser(request);
            }
            count++;
        }
        if (count == 0) {
            throw new BusinessException("导入用户没有有效数据行");
        }
        return new ImportResult(count, skipped);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserSaveRequest request) {
        ensureUniqueUserName(request.userName(), null);

        SysUser user = new SysUser();
        fillUser(user, request);
        String password = defaultIfBlank(request.password(), "123456");
        if (!Objects.equals(password, "123456")) {
            loginSecurityService.validatePasswordStrength(password);
        }
        user.setPassword(passwordEncoder.encode(password));
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
            loginSecurityService.validatePasswordStrength(request.password());
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

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(UserStatusRequest request) {
        SysUser user = findUser(request.id());
        user.setStatus(request.status());
        user.setUpdateBy("system");
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(UserResetPasswordRequest request) {
        SysUser user = findUser(request.id());
        loginSecurityService.validatePasswordStrength(request.password());
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

    private UserExcel toUserExcel(SysUser user) {
        UserExcel excel = new UserExcel();
        excel.userId = user.getUserId();
        excel.userName = user.getUserName();
        excel.nickName = user.getNickName();
        excel.userPhone = user.getPhonenumber();
        excel.userEmail = user.getEmail();
        excel.userGender = sexLabel(user.getSex());
        excel.status = user.getStatus();
        excel.userRoles = String.join(",", userMapper.selectRoleCodesByUserId(user.getUserId()));
        excel.createTime = formatDateTime(user.getCreateTime());
        return excel;
    }

    private SysUser findImportUser(UserExcel row) {
        if (row.userId != null) {
            SysUser user = userMapper.selectById(row.userId);
            if (user != null && Objects.equals(user.getDelFlag(), "0")) {
                return user;
            }
        }
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, row.userName)
                .eq(SysUser::getDelFlag, "0")
                .last("LIMIT 1"));
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

    private List<String> parseRoles(String roles) {
        if (!hasText(roles)) {
            return List.of();
        }
        return ROLE_SPLITTER.splitAsStream(roles)
                .filter(this::hasText)
                .toList();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    private String sexLabel(String sex) {
        if (Objects.equals(sex, "0")) return "男";
        if (Objects.equals(sex, "1")) return "女";
        return "未知";
    }

    private String normalizeSex(String gender) {
        if (Objects.equals(gender, "女") || Objects.equals(gender, "1") || Objects.equals(gender, "2")) {
            return "1";
        }
        return "0";
    }

    private long parseLong(String value, long fallback) {
        if (!hasText(value)) return fallback;
        try { return Long.parseLong(value); } catch (NumberFormatException ignored) { return fallback; }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }
}

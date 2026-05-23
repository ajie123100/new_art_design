package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.AppRouteRecord;
import com.artdesign.system.domain.dto.RoleListItem;
import com.artdesign.system.domain.dto.RoleMenuRequest;
import com.artdesign.system.domain.dto.RoleSaveRequest;
import com.artdesign.system.domain.dto.RoleStatusRequest;
import com.artdesign.system.domain.entity.SysRole;
import com.artdesign.system.mapper.SysRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class SysRoleService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysRoleMapper roleMapper;
    private final SysMenuService menuService;

    public SysRoleService(SysRoleMapper roleMapper, SysMenuService menuService) {
        this.roleMapper = roleMapper;
        this.menuService = menuService;
    }

    public PageResult<RoleListItem> listRoles(Map<String, String> params) {
        long current = parseLong(params.get("current"), 1L);
        long size = parseLong(params.get("size"), 10L);

        List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .like(hasText(params.get("roleName")), SysRole::getRoleName, params.get("roleName"))
                .like(hasText(params.get("roleCode")), SysRole::getRoleCode, params.get("roleCode"))
                .eq(hasText(params.get("enabled")), SysRole::getStatus, booleanToStatus(params.get("enabled")))
                .eq(SysRole::getDelFlag, "0")
                .orderByAsc(SysRole::getRoleSort)
                .orderByAsc(SysRole::getRoleId));

        List<RoleListItem> records = roles.stream()
                .map(this::toRoleListItem)
                .toList();
        return page(records, current, size);
    }

    public RoleListItem getRole(Long roleId) {
        return toRoleListItem(findRole(roleId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createRole(RoleSaveRequest request) {
        ensureUniqueRoleCode(request.roleCode(), null);
        SysRole role = new SysRole();
        fillRole(role, request);
        role.setRoleSort(nextRoleSort());
        role.setStatus(booleanToStatus(request.enabled()));
        role.setDelFlag("0");
        role.setCreateBy("system");
        role.setCreateTime(LocalDateTime.now());
        roleMapper.insert(role);
        return role.getRoleId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleSaveRequest request) {
        if (request.roleId() == null) {
            throw new BusinessException("角色ID不能为空");
        }
        SysRole role = findRole(request.roleId());
        ensureUniqueRoleCode(request.roleCode(), request.roleId());
        fillRole(role, request);
        role.setStatus(booleanToStatus(request.enabled()));
        role.setUpdateBy("system");
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException("请选择要删除的角色");
        }
        for (Long roleId : roleIds) {
            if (Objects.equals(roleId, 1L)) {
                throw new BusinessException("不能删除超级管理员角色");
            }
            SysRole role = findRole(roleId);
            role.setDelFlag("1");
            role.setUpdateBy("system");
            role.setUpdateTime(LocalDateTime.now());
            roleMapper.updateById(role);
            roleMapper.deleteRoleMenus(roleId);
        }
    }

    public void updateRoleStatus(RoleStatusRequest request) {
        SysRole role = findRole(request.roleId());
        role.setStatus(booleanToStatus(request.enabled()));
        role.setUpdateBy("system");
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);
    }

    public List<AppRouteRecord> getGrantMenuTree() {
        return menuService.getGrantMenuTree();
    }

    public List<Long> getRoleMenuIds(Long roleId) {
        findRole(roleId);
        return roleMapper.selectMenuIdsByRoleId(roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveRoleMenus(RoleMenuRequest request) {
        findRole(request.roleId());
        roleMapper.deleteRoleMenus(request.roleId());
        if (request.menuIds() == null || request.menuIds().isEmpty()) {
            return;
        }
        for (Long menuId : request.menuIds()) {
            roleMapper.insertRoleMenu(request.roleId(), menuId);
        }
    }

    private SysRole findRole(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null || !Objects.equals(role.getDelFlag(), "0")) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    private RoleListItem toRoleListItem(SysRole role) {
        return new RoleListItem(
                role.getRoleId(),
                role.getRoleName(),
                role.getRoleCode(),
                role.getRemark(),
                Objects.equals(role.getStatus(), "1"),
                formatDateTime(role.getCreateTime())
        );
    }

    private void fillRole(SysRole role, RoleSaveRequest request) {
        role.setRoleName(request.roleName());
        role.setRoleCode(request.roleCode());
        role.setRemark(defaultIfBlank(request.description(), ""));
    }

    private void ensureUniqueRoleCode(String roleCode, Long ignoredRoleId) {
        SysRole existing = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode)
                .eq(SysRole::getDelFlag, "0")
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getRoleId(), ignoredRoleId)) {
            throw new BusinessException("角色编码已存在");
        }
    }

    private int nextRoleSort() {
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getDelFlag, "0"));
        return count.intValue() + 1;
    }

    private <T> PageResult<T> page(List<T> records, long current, long size) {
        int from = (int) Math.min(Math.max(current - 1, 0) * size, records.size());
        int to = (int) Math.min(from + size, records.size());
        return PageResult.of(records.subList(from, to), current, size, records.size());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    private String booleanToStatus(String enabled) {
        return Objects.equals(enabled, "true") ? "1" : "2";
    }

    private String booleanToStatus(Boolean enabled) {
        return Boolean.FALSE.equals(enabled) ? "2" : "1";
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

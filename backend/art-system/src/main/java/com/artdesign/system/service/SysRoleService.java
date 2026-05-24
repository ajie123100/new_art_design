package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.core.page.PageUtils;
import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.AppRouteRecord;
import com.artdesign.system.domain.dto.ImportResult;
import com.artdesign.system.domain.dto.RoleExcel;
import com.artdesign.system.domain.dto.RoleListItem;
import com.artdesign.system.domain.dto.RoleMenuRequest;
import com.artdesign.system.domain.dto.RoleSaveRequest;
import com.artdesign.system.domain.dto.RoleStatusRequest;
import com.artdesign.system.domain.entity.SysRole;
import com.artdesign.system.mapper.SysRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class SysRoleService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern ID_SPLITTER = Pattern.compile("[,，\\s]+");

    private final SysRoleMapper roleMapper;
    private final SysMenuService menuService;

    public SysRoleService(SysRoleMapper roleMapper, SysMenuService menuService) {
        this.roleMapper = roleMapper;
        this.menuService = menuService;
    }

    public PageResult<RoleListItem> listRoles(Map<String, String> params) {
        long pageNum = PageUtils.pageNum(params);
        long pageSize = PageUtils.pageSize(params);

        Page<SysRole> page = new Page<>(pageNum, pageSize);
        IPage<SysRole> result = roleMapper.selectPage(page, new LambdaQueryWrapper<SysRole>()
                .like(hasText(params.get("roleName")), SysRole::getRoleName, params.get("roleName"))
                .like(hasText(params.get("roleCode")), SysRole::getRoleCode, params.get("roleCode"))
                .eq(hasText(params.get("enabled")), SysRole::getStatus, booleanToStatus(params.get("enabled")))
                .eq(SysRole::getDelFlag, "0")
                .orderByAsc(SysRole::getRoleSort)
                .orderByAsc(SysRole::getRoleId));

        List<RoleListItem> records = result.getRecords().stream()
                .map(this::toRoleListItem)
                .toList();
        return new PageResult<>(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    public RoleListItem getRole(Long roleId) {
        return toRoleListItem(findRole(roleId));
    }

    public List<RoleExcel> exportRoles(Map<String, String> params) {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .like(hasText(params.get("roleName")), SysRole::getRoleName, params.get("roleName"))
                        .like(hasText(params.get("roleCode")), SysRole::getRoleCode, params.get("roleCode"))
                        .eq(hasText(params.get("enabled")), SysRole::getStatus, booleanToStatus(params.get("enabled")))
                        .eq(SysRole::getDelFlag, "0")
                        .orderByAsc(SysRole::getRoleSort)
                        .orderByAsc(SysRole::getRoleId))
                .stream()
                .map(this::toRoleExcel)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public ImportResult importRoles(List<RoleExcel> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new BusinessException("导入角色不能为空");
        }
        int count = 0;
        int skipped = 0;
        for (RoleExcel row : rows) {
            if (row == null || !hasText(row.roleCode) || !hasText(row.roleName)) {
                skipped++;
                continue;
            }
            SysRole existing = findImportRole(row);
            RoleSaveRequest request = new RoleSaveRequest(
                    existing == null ? row.roleId : existing.getRoleId(),
                    row.roleName,
                    row.roleCode,
                    row.remark,
                    defaultIfBlank(row.dataScope, "1"),
                    parseIds(row.deptIds),
                    parseBoolean(row.enabled, true)
            );
            Long roleId;
            if (existing == null) {
                roleId = createRole(request);
            } else {
                updateRole(request);
                roleId = existing.getRoleId();
            }
            if (row.roleSort != null) {
                SysRole role = findRole(roleId);
                role.setRoleSort(row.roleSort);
                roleMapper.updateById(role);
            }
            if (row.menuIds != null) {
                saveRoleMenus(new RoleMenuRequest(roleId, parseIds(row.menuIds)));
            }
            count++;
        }
        if (count == 0) {
            throw new BusinessException("导入角色没有有效数据行");
        }
        return new ImportResult(count, skipped);
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
        saveRoleDepts(role.getRoleId(), request);
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
        saveRoleDepts(role.getRoleId(), request);
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
            roleMapper.deleteRoleDepts(roleId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
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

    public List<Long> getRoleDeptIds(Long roleId) {
        findRole(roleId);
        return roleMapper.selectDeptIdsByRoleId(roleId);
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
                role.getDataScope(),
                Objects.equals(role.getStatus(), "1"),
                formatDateTime(role.getCreateTime())
        );
    }

    private RoleExcel toRoleExcel(SysRole role) {
        RoleExcel excel = new RoleExcel();
        excel.roleId = role.getRoleId();
        excel.roleName = role.getRoleName();
        excel.roleCode = role.getRoleCode();
        excel.roleSort = role.getRoleSort();
        excel.dataScope = role.getDataScope();
        excel.enabled = Objects.equals(role.getStatus(), "1") ? "是" : "否";
        excel.deptIds = joinIds(roleMapper.selectDeptIdsByRoleId(role.getRoleId()));
        excel.menuIds = joinIds(roleMapper.selectMenuIdsByRoleId(role.getRoleId()));
        excel.remark = role.getRemark();
        excel.createTime = formatDateTime(role.getCreateTime());
        return excel;
    }

    private SysRole findImportRole(RoleExcel row) {
        if (row.roleId != null) {
            SysRole role = roleMapper.selectById(row.roleId);
            if (role != null && Objects.equals(role.getDelFlag(), "0")) {
                return role;
            }
        }
        return roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, row.roleCode)
                .eq(SysRole::getDelFlag, "0")
                .last("LIMIT 1"));
    }

    private void fillRole(SysRole role, RoleSaveRequest request) {
        role.setRoleName(request.roleName());
        role.setRoleCode(request.roleCode());
        role.setDataScope(defaultIfBlank(request.dataScope(), "1"));
        role.setRemark(defaultIfBlank(request.description(), ""));
    }

    private void saveRoleDepts(Long roleId, RoleSaveRequest request) {
        roleMapper.deleteRoleDepts(roleId);
        if (!Objects.equals(request.dataScope(), "2") || request.deptIds() == null || request.deptIds().isEmpty()) {
            return;
        }
        for (Long deptId : request.deptIds()) {
            roleMapper.insertRoleDept(roleId, deptId);
        }
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

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    private String booleanToStatus(String enabled) {
        return Objects.equals(enabled, "true") ? "1" : "2";
    }

    private String booleanToStatus(Boolean enabled) {
        return Boolean.FALSE.equals(enabled) ? "2" : "1";
    }

    private List<Long> parseIds(String ids) {
        if (!hasText(ids)) {
            return List.of();
        }
        return ID_SPLITTER.splitAsStream(ids)
                .filter(this::hasText)
                .map(Long::valueOf)
                .toList();
    }

    private String joinIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        return ids.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
    }

    private Boolean parseBoolean(String value, boolean fallback) {
        if (!hasText(value)) {
            return fallback;
        }
        return Objects.equals(value, "1")
                || Objects.equals(value, "是")
                || Objects.equals(value, "启用")
                || Objects.equals(value, "true")
                || Objects.equals(value, "TRUE");
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

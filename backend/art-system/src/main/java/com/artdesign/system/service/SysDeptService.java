package com.artdesign.system.service;

import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.DeptSaveRequest;
import com.artdesign.system.domain.dto.DeptStatusRequest;
import com.artdesign.system.domain.dto.DeptTreeItem;
import com.artdesign.system.domain.entity.SysDept;
import com.artdesign.system.domain.entity.SysUser;
import com.artdesign.system.mapper.SysDeptMapper;
import com.artdesign.system.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SysDeptService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;

    public SysDeptService(SysDeptMapper deptMapper, SysUserMapper userMapper) {
        this.deptMapper = deptMapper;
        this.userMapper = userMapper;
    }

    public List<DeptTreeItem> listDepartments(Map<String, String> params) {
        List<SysDept> departments = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .like(hasText(params.get("deptName")), SysDept::getDeptName, params.get("deptName"))
                .like(hasText(params.get("leader")), SysDept::getLeader, params.get("leader"))
                .eq(hasText(params.get("enabled")), SysDept::getStatus, booleanToStatus(params.get("enabled")))
                .eq(SysDept::getDelFlag, "0")
                .orderByAsc(SysDept::getParentId)
                .orderByAsc(SysDept::getOrderNum)
                .orderByAsc(SysDept::getDeptId));

        if (hasAnyFilter(params)) {
            return departments.stream()
                    .map(this::toDeptTreeItem)
                    .sorted(Comparator.comparing((DeptTreeItem item) -> item.getOrderNum() == null ? 0 : item.getOrderNum())
                            .thenComparing(DeptTreeItem::getDeptId))
                    .toList();
        }

        return buildDeptTree(departments);
    }

    public DeptTreeItem getDepartment(Long deptId) {
        return toDeptTreeItem(findDepartment(deptId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createDepartment(DeptSaveRequest request) {
        ensureValidParent(request.parentId(), null);

        SysDept dept = new SysDept();
        fillDepartment(dept, request);
        dept.setAncestors(resolveAncestors(dept.getParentId()));
        dept.setDelFlag("0");
        dept.setCreateBy("system");
        dept.setCreateTime(LocalDateTime.now());
        deptMapper.insert(dept);
        return dept.getDeptId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDepartment(DeptSaveRequest request) {
        if (request.deptId() == null) {
            throw new BusinessException("部门ID不能为空");
        }
        SysDept dept = findDepartment(request.deptId());
        Long oldParentId = dept.getParentId();
        String oldAncestors = dept.getAncestors();

        ensureValidParent(request.parentId(), request.deptId());
        fillDepartment(dept, request);
        dept.setAncestors(resolveAncestors(dept.getParentId()));
        dept.setUpdateBy("system");
        dept.setUpdateTime(LocalDateTime.now());
        deptMapper.updateById(dept);

        if (!Objects.equals(oldParentId, dept.getParentId()) || !Objects.equals(oldAncestors, dept.getAncestors())) {
            updateChildrenAncestors(dept, oldAncestors);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartments(List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            throw new BusinessException("请选择要删除的部门");
        }
        for (Long deptId : deptIds) {
            SysDept dept = findDepartment(deptId);
            Long childCount = deptMapper.selectCount(new LambdaQueryWrapper<SysDept>()
                    .eq(SysDept::getParentId, deptId)
                    .eq(SysDept::getDelFlag, "0"));
            if (childCount > 0) {
                throw new BusinessException("存在子部门，不能删除");
            }
            Long userCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getDeptId, deptId)
                    .eq(SysUser::getDelFlag, "0"));
            if (userCount > 0) {
                throw new BusinessException("部门下存在用户，不能删除");
            }
            dept.setDelFlag("1");
            dept.setUpdateBy("system");
            dept.setUpdateTime(LocalDateTime.now());
            deptMapper.updateById(dept);
        }
    }

    public void updateStatus(DeptStatusRequest request) {
        SysDept dept = findDepartment(request.deptId());
        dept.setStatus(booleanToStatus(request.enabled()));
        dept.setUpdateBy("system");
        dept.setUpdateTime(LocalDateTime.now());
        deptMapper.updateById(dept);
    }

    private SysDept findDepartment(Long deptId) {
        SysDept dept = deptMapper.selectById(deptId);
        if (dept == null || !Objects.equals(dept.getDelFlag(), "0")) {
            throw new BusinessException("部门不存在");
        }
        return dept;
    }

    private void fillDepartment(SysDept dept, DeptSaveRequest request) {
        dept.setParentId(request.parentId() == null ? 0L : request.parentId());
        dept.setDeptName(request.deptName());
        dept.setOrderNum(request.orderNum() == null ? 1 : request.orderNum());
        dept.setLeader(defaultIfBlank(request.leader(), ""));
        dept.setPhone(defaultIfBlank(request.phone(), ""));
        dept.setEmail(defaultIfBlank(request.email(), ""));
        dept.setStatus(booleanToStatus(request.enabled()));
    }

    private void ensureValidParent(Long parentId, Long currentDeptId) {
        if (parentId == null || parentId == 0) {
            return;
        }
        if (Objects.equals(parentId, currentDeptId)) {
            throw new BusinessException("上级部门不能选择自己");
        }

        SysDept parent = findDepartment(parentId);
        Long nextParentId = parent.getParentId();
        while (nextParentId != null && nextParentId != 0) {
            if (Objects.equals(nextParentId, currentDeptId)) {
                throw new BusinessException("上级部门不能选择自己的子级");
            }
            SysDept next = findDepartment(nextParentId);
            nextParentId = next.getParentId();
        }
    }

    private String resolveAncestors(Long parentId) {
        if (parentId == null || parentId == 0) {
            return "0";
        }
        SysDept parent = findDepartment(parentId);
        return defaultIfBlank(parent.getAncestors(), "0") + "," + parent.getDeptId();
    }

    private void updateChildrenAncestors(SysDept dept, String oldAncestors) {
        List<SysDept> children = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .likeRight(SysDept::getAncestors, defaultIfBlank(oldAncestors, "0") + "," + dept.getDeptId())
                .eq(SysDept::getDelFlag, "0"));
        String newPrefix = dept.getAncestors() + "," + dept.getDeptId();
        String oldPrefix = defaultIfBlank(oldAncestors, "0") + "," + dept.getDeptId();
        for (SysDept child : children) {
            child.setAncestors(defaultIfBlank(child.getAncestors(), "").replaceFirst("^" + oldPrefix, newPrefix));
            child.setUpdateBy("system");
            child.setUpdateTime(LocalDateTime.now());
            deptMapper.updateById(child);
        }
    }

    private List<DeptTreeItem> buildDeptTree(List<SysDept> departments) {
        Map<Long, DeptTreeItem> deptMap = departments.stream()
                .map(this::toDeptTreeItem)
                .collect(Collectors.toMap(DeptTreeItem::getDeptId, Function.identity(), (left, right) -> left));

        List<DeptTreeItem> roots = new ArrayList<>();
        Set<Long> validIds = deptMap.keySet();
        for (SysDept dept : departments) {
            DeptTreeItem item = deptMap.get(dept.getDeptId());
            Long parentId = dept.getParentId();
            if (parentId == null || parentId == 0 || !validIds.contains(parentId)) {
                roots.add(item);
                continue;
            }

            DeptTreeItem parent = deptMap.get(parentId);
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(item);
        }

        sortDeptItems(roots);
        return roots;
    }

    private void sortDeptItems(List<DeptTreeItem> items) {
        items.sort(Comparator.comparing((DeptTreeItem item) -> item.getOrderNum() == null ? 0 : item.getOrderNum())
                .thenComparing(DeptTreeItem::getDeptId));
        for (DeptTreeItem item : items) {
            if (item.getChildren() != null) {
                sortDeptItems(item.getChildren());
            }
        }
    }

    private DeptTreeItem toDeptTreeItem(SysDept dept) {
        DeptTreeItem item = new DeptTreeItem();
        item.setDeptId(dept.getDeptId());
        item.setParentId(dept.getParentId());
        item.setAncestors(dept.getAncestors());
        item.setDeptName(dept.getDeptName());
        item.setOrderNum(dept.getOrderNum());
        item.setLeader(dept.getLeader());
        item.setPhone(dept.getPhone());
        item.setEmail(dept.getEmail());
        item.setEnabled(Objects.equals(dept.getStatus(), "1"));
        item.setCreateTime(formatDateTime(dept.getCreateTime()));
        item.setUpdateTime(formatDateTime(dept.getUpdateTime()));
        return item;
    }

    private boolean hasAnyFilter(Map<String, String> params) {
        return hasText(params.get("deptName")) || hasText(params.get("leader")) || hasText(params.get("enabled"));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    private String booleanToStatus(Boolean enabled) {
        return Boolean.FALSE.equals(enabled) ? "2" : "1";
    }

    private String booleanToStatus(String enabled) {
        return Objects.equals(enabled, "true") ? "1" : "2";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }
}

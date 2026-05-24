package com.artdesign.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.artdesign.system.domain.entity.SysDept;
import com.artdesign.system.mapper.SysDeptMapper;
import com.artdesign.system.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class DataScopeService {
    private static final Long SUPER_ADMIN_ID = 1L;
    private static final String DATA_SCOPE_ALL = "1";
    private static final String DATA_SCOPE_CUSTOM = "2";
    private static final String DATA_SCOPE_DEPT = "3";
    private static final String DATA_SCOPE_DEPT_AND_CHILD = "4";
    private static final String DATA_SCOPE_SELF = "5";
    private static final String SUPER_ROLE = "R_SUPER";

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;

    public DataScopeService(SysUserMapper userMapper, SysDeptMapper deptMapper) {
        this.userMapper = userMapper;
        this.deptMapper = deptMapper;
    }

    public DataScope currentDataScope() {
        if (!StpUtil.isLogin()) {
            return DataScope.restrictedToSelf(null);
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (Objects.equals(userId, SUPER_ADMIN_ID) || userMapper.selectRoleCodesByUserId(userId).contains(SUPER_ROLE)) {
            return DataScope.unrestricted(userId);
        }

        List<String> scopes = userMapper.selectRoleDataScopesByUserId(userId);
        if (scopes.contains(DATA_SCOPE_ALL)) {
            return DataScope.unrestricted(userId);
        }

        Long deptId = userMapper.selectDeptIdByUserId(userId);
        Set<Long> deptIds = new HashSet<>();
        if (scopes.contains(DATA_SCOPE_CUSTOM)) {
            deptIds.addAll(userMapper.selectCustomDeptIdsByUserId(userId));
        }
        if (deptId != null && scopes.contains(DATA_SCOPE_DEPT)) {
            deptIds.add(deptId);
        }
        if (deptId != null && scopes.contains(DATA_SCOPE_DEPT_AND_CHILD)) {
            deptIds.add(deptId);
            deptIds.addAll(selectChildDeptIds(deptId));
        }
        if (!deptIds.isEmpty()) {
            return DataScope.restrictedToDepartments(userId, deptIds.stream().toList());
        }
        if (deptId != null && scopes.contains(DATA_SCOPE_SELF)) {
            return DataScope.restrictedToSelf(userId, deptId);
        }
        return DataScope.restrictedToSelf(userId);
    }

    private List<Long> selectChildDeptIds(Long deptId) {
        return deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                        .apply("FIND_IN_SET({0}, ancestors)", deptId)
                        .eq(SysDept::getDelFlag, "0"))
                .stream()
                .map(SysDept::getDeptId)
                .toList();
    }

    public record DataScope(Long userId, boolean unrestricted, boolean selfOnly, List<Long> deptIds) {
        static DataScope unrestricted(Long userId) {
            return new DataScope(userId, true, false, List.of());
        }

        static DataScope restrictedToDepartments(Long userId, List<Long> deptIds) {
            return new DataScope(userId, false, false, deptIds == null ? List.of() : deptIds);
        }

        static DataScope restrictedToSelf(Long userId) {
            return new DataScope(userId, false, true, List.of());
        }

        static DataScope restrictedToSelf(Long userId, Long deptId) {
            return new DataScope(userId, false, true, deptId == null ? List.of() : List.of(deptId));
        }

        public boolean hasDeptLimit() {
            return !unrestricted && !deptIds.isEmpty();
        }
    }
}

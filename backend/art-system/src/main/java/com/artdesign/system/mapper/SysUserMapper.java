package com.artdesign.system.mapper;

import com.artdesign.system.domain.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    @Select("""
            SELECT r.role_code
            FROM sys_role r
            INNER JOIN sys_user_role ur ON ur.role_id = r.role_id
            WHERE ur.user_id = #{userId}
              AND r.status = '1'
              AND r.del_flag = '0'
            ORDER BY r.role_sort ASC
            """)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT r.data_scope
            FROM sys_role r
            INNER JOIN sys_user_role ur ON ur.role_id = r.role_id
            WHERE ur.user_id = #{userId}
              AND r.status = '1'
              AND r.del_flag = '0'
            ORDER BY r.role_sort ASC
            """)
    List<String> selectRoleDataScopesByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT dept_id
            FROM sys_user
            WHERE user_id = #{userId}
              AND del_flag = '0'
            LIMIT 1
            """)
    Long selectDeptIdByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT rd.dept_id
            FROM sys_role_dept rd
            INNER JOIN sys_user_role ur ON ur.role_id = rd.role_id
            INNER JOIN sys_role r ON r.role_id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND r.status = '1'
              AND r.del_flag = '0'
              AND r.data_scope = '2'
            """)
    List<Long> selectCustomDeptIdsByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT r.role_name
            FROM sys_role r
            INNER JOIN sys_user_role ur ON ur.role_id = r.role_id
            WHERE ur.user_id = #{userId}
              AND r.status = '1'
              AND r.del_flag = '0'
            ORDER BY r.role_sort ASC
            """)
    List<String> selectRoleNamesByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT m.perms
            FROM sys_menu m
            INNER JOIN sys_role_menu rm ON rm.menu_id = m.menu_id
            INNER JOIN sys_user_role ur ON ur.role_id = rm.role_id
            INNER JOIN sys_role r ON r.role_id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND m.menu_type = 'F'
              AND m.status = '1'
              AND r.status = '1'
              AND r.del_flag = '0'
              AND m.perms IS NOT NULL
              AND m.perms <> ''
            ORDER BY m.perms ASC
            """)
    List<String> selectButtonsByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT m.perms
            FROM sys_menu m
            INNER JOIN sys_role_menu rm ON rm.menu_id = m.menu_id
            INNER JOIN sys_user_role ur ON ur.role_id = rm.role_id
            INNER JOIN sys_role r ON r.role_id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND m.menu_type IN ('C', 'F')
              AND m.status = '1'
              AND r.status = '1'
              AND r.del_flag = '0'
              AND m.perms IS NOT NULL
              AND m.perms <> ''
            ORDER BY m.perms ASC
            """)
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    @Select("""
            <script>
            SELECT role_id
            FROM sys_role
            WHERE role_code IN
            <foreach collection="roleCodes" item="roleCode" open="(" separator="," close=")">
                #{roleCode}
            </foreach>
              AND del_flag = '0'
            </script>
            """)
    List<Long> selectRoleIdsByRoleCodes(@Param("roleCodes") List<String> roleCodes);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);

    @Insert("INSERT INTO sys_user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}

package com.artdesign.system.mapper;

import com.artdesign.system.domain.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    @Select("""
            SELECT DISTINCT m.*
            FROM sys_menu m
            LEFT JOIN sys_role_menu rm ON rm.menu_id = m.menu_id
            LEFT JOIN sys_user_role ur ON ur.role_id = rm.role_id
            LEFT JOIN sys_role r ON r.role_id = ur.role_id
            WHERE m.status = '1'
              AND m.menu_type IN ('M', 'C')
              AND (
                #{superAdmin} = true
                OR (
                  ur.user_id = #{userId}
                  AND r.status = '1'
                  AND r.del_flag = '0'
                )
              )
            ORDER BY m.parent_id ASC, m.order_num ASC, m.menu_id ASC
            """)
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId, @Param("superAdmin") boolean superAdmin);

    @Select("""
            SELECT *
            FROM sys_menu
            WHERE status = '1'
            ORDER BY parent_id ASC, order_num ASC, menu_id ASC
            """)
    List<SysMenu> selectAllGrantMenus();
}

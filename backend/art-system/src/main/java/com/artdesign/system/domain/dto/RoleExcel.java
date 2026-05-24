package com.artdesign.system.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public class RoleExcel {
    @ExcelProperty("角色ID")
    public Long roleId;
    @ExcelProperty("角色名称")
    public String roleName;
    @ExcelProperty("角色编码")
    public String roleCode;
    @ExcelProperty("显示顺序")
    public Integer roleSort;
    @ExcelProperty("数据范围")
    public String dataScope;
    @ExcelProperty("启用")
    public String enabled;
    @ExcelProperty("部门ID")
    public String deptIds;
    @ExcelProperty("菜单ID")
    public String menuIds;
    @ExcelProperty("备注")
    public String remark;
    @ExcelProperty("创建时间")
    public String createTime;

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public Integer getRoleSort() { return roleSort; }
    public void setRoleSort(Integer roleSort) { this.roleSort = roleSort; }
    public String getDataScope() { return dataScope; }
    public void setDataScope(String dataScope) { this.dataScope = dataScope; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public String getDeptIds() { return deptIds; }
    public void setDeptIds(String deptIds) { this.deptIds = deptIds; }
    public String getMenuIds() { return menuIds; }
    public void setMenuIds(String menuIds) { this.menuIds = menuIds; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}

package com.artdesign.system.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public class MenuExcel {
    @ExcelProperty("菜单ID")
    public Long menuId;
    @ExcelProperty("父菜单ID")
    public Long parentId;
    @ExcelProperty("菜单名称")
    public String menuName;
    @ExcelProperty("显示顺序")
    public Integer orderNum;
    @ExcelProperty("路由地址")
    public String path;
    @ExcelProperty("组件路径")
    public String component;
    @ExcelProperty("路由名称")
    public String routeName;
    @ExcelProperty("是否外链")
    public String external;
    @ExcelProperty("是否缓存")
    public String keepAlive;
    @ExcelProperty("菜单类型")
    public String menuType;
    @ExcelProperty("是否显示")
    public String visible;
    @ExcelProperty("是否启用")
    public String enabled;
    @ExcelProperty("权限标识")
    public String perms;
    @ExcelProperty("图标")
    public String icon;
    @ExcelProperty("备注")
    public String remark;

    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public String getExternal() { return external; }
    public void setExternal(String external) { this.external = external; }
    public String getKeepAlive() { return keepAlive; }
    public void setKeepAlive(String keepAlive) { this.keepAlive = keepAlive; }
    public String getMenuType() { return menuType; }
    public void setMenuType(String menuType) { this.menuType = menuType; }
    public String getVisible() { return visible; }
    public void setVisible(String visible) { this.visible = visible; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public String getPerms() { return perms; }
    public void setPerms(String perms) { this.perms = perms; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

package com.artdesign.system.service;

import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.AppRouteRecord;
import com.artdesign.system.domain.dto.MenuSaveRequest;
import com.artdesign.system.domain.dto.MenuStatusRequest;
import com.artdesign.system.domain.dto.MenuTreeItem;
import com.artdesign.system.domain.dto.RouteMeta;
import com.artdesign.system.domain.entity.SysMenu;
import com.artdesign.system.mapper.SysMenuMapper;
import com.artdesign.system.mapper.SysRoleMapper;
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
public class SysMenuService {
    private static final String SUPER_ROLE = "R_SUPER";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;

    public SysMenuService(SysMenuMapper menuMapper, SysRoleMapper roleMapper, SysUserMapper userMapper) {
        this.menuMapper = menuMapper;
        this.roleMapper = roleMapper;
        this.userMapper = userMapper;
    }

    public List<MenuTreeItem> listSystemMenus(Map<String, String> params) {
        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getOrderNum)
                .orderByAsc(SysMenu::getMenuId));
        List<MenuTreeItem> tree = buildMenuTree(menus);
        return filterMenuTree(tree, params);
    }

    public MenuTreeItem getSystemMenu(Long menuId) {
        return toMenuTreeItem(findMenu(menuId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createMenu(MenuSaveRequest request) {
        ensureValidParent(request.parentId(), null);
        SysMenu menu = new SysMenu();
        fillMenu(menu, request);
        menu.setCreateBy("system");
        menu.setCreateTime(LocalDateTime.now());
        menuMapper.insert(menu);
        return menu.getMenuId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(MenuSaveRequest request) {
        if (request.menuId() == null) {
            throw new BusinessException("菜单ID不能为空");
        }
        SysMenu menu = findMenu(request.menuId());
        ensureValidParent(request.parentId(), request.menuId());
        fillMenu(menu, request);
        menu.setUpdateBy("system");
        menu.setUpdateTime(LocalDateTime.now());
        menuMapper.updateById(menu);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMenus(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            throw new BusinessException("请选择要删除的菜单");
        }
        for (Long menuId : menuIds) {
            findMenu(menuId);
            Long childCount = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getParentId, menuId));
            if (childCount > 0) {
                throw new BusinessException("存在子菜单，不能删除");
            }
            menuMapper.deleteById(menuId);
            roleMapper.deleteRoleMenusByMenuId(menuId);
        }
    }

    public void updateMenuStatus(MenuStatusRequest request) {
        SysMenu menu = findMenu(request.menuId());
        menu.setStatus(booleanToStatus(request.enabled()));
        menu.setUpdateBy("system");
        menu.setUpdateTime(LocalDateTime.now());
        menuMapper.updateById(menu);
    }

    public List<AppRouteRecord> getGrantMenuTree() {
        return buildRouteTree(menuMapper.selectAllGrantMenus());
    }

    public List<AppRouteRecord> getMenuTree(Long userId) {
        List<String> roles = userMapper.selectRoleCodesByUserId(userId);
        boolean superAdmin = roles.contains(SUPER_ROLE);
        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId, superAdmin);
        return buildRouteTree(menus);
    }

    private SysMenu findMenu(Long menuId) {
        SysMenu menu = menuMapper.selectById(menuId);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return menu;
    }

    private void ensureValidParent(Long parentId, Long currentMenuId) {
        if (parentId == null || parentId == 0) {
            return;
        }
        if (Objects.equals(parentId, currentMenuId)) {
            throw new BusinessException("上级菜单不能选择自己");
        }

        SysMenu parent = findMenu(parentId);
        Long nextParentId = parent.getParentId();
        while (nextParentId != null && nextParentId != 0) {
            if (Objects.equals(nextParentId, currentMenuId)) {
                throw new BusinessException("上级菜单不能选择自己的子级");
            }
            SysMenu next = findMenu(nextParentId);
            nextParentId = next.getParentId();
        }
    }

    private MenuTreeItem toMenuTreeItem(SysMenu menu) {
        MenuTreeItem item = new MenuTreeItem();
        item.setMenuId(menu.getMenuId());
        item.setParentId(menu.getParentId());
        item.setMenuName(menu.getMenuName());
        item.setOrderNum(menu.getOrderNum());
        item.setPath(menu.getPath());
        item.setComponent(menu.getComponent());
        item.setRouteName(menu.getRouteName());
        item.setExternal(Objects.equals(menu.getIsFrame(), "0"));
        item.setKeepAlive(Objects.equals(menu.getIsCache(), "0"));
        item.setMenuType(menu.getMenuType());
        item.setVisible(!Objects.equals(menu.getVisible(), "2"));
        item.setEnabled(Objects.equals(menu.getStatus(), "1"));
        item.setPerms(menu.getPerms());
        item.setIcon(menu.getIcon());
        item.setRemark(menu.getRemark());
        item.setCreateTime(formatDateTime(menu.getCreateTime()));
        item.setUpdateTime(formatDateTime(menu.getUpdateTime()));
        return item;
    }

    private void fillMenu(SysMenu menu, MenuSaveRequest request) {
        String menuType = normalizeMenuType(request.menuType());
        if (!Objects.equals(menuType, "F") && !hasText(request.path())) {
            throw new BusinessException("路由地址不能为空");
        }

        menu.setMenuName(request.menuName());
        menu.setParentId(request.parentId() == null ? 0L : request.parentId());
        menu.setOrderNum(request.orderNum() == null ? 1 : request.orderNum());
        menu.setPath(defaultIfBlank(request.path(), ""));
        menu.setComponent(defaultIfBlank(request.component(), ""));
        menu.setRouteName(defaultIfBlank(request.routeName(), deriveRouteName(request.path(), request.menuName())));
        menu.setIsFrame(Boolean.TRUE.equals(request.external()) ? "0" : "1");
        menu.setIsCache(Boolean.FALSE.equals(request.keepAlive()) ? "1" : "0");
        menu.setMenuType(menuType);
        menu.setVisible(Boolean.FALSE.equals(request.visible()) ? "2" : "1");
        menu.setStatus(booleanToStatus(request.enabled()));
        menu.setPerms(defaultIfBlank(request.perms(), ""));
        menu.setIcon(defaultIfBlank(request.icon(), ""));
        menu.setRemark(defaultIfBlank(request.remark(), ""));
    }

    private List<MenuTreeItem> buildMenuTree(List<SysMenu> menus) {
        Map<Long, MenuTreeItem> menuMap = menus.stream()
                .map(this::toMenuTreeItem)
                .collect(Collectors.toMap(MenuTreeItem::getMenuId, Function.identity(), (left, right) -> left));

        List<MenuTreeItem> roots = new ArrayList<>();
        Set<Long> validIds = menuMap.keySet();
        for (SysMenu menu : menus) {
            MenuTreeItem item = menuMap.get(menu.getMenuId());
            Long parentId = menu.getParentId();
            if (parentId == null || parentId == 0 || !validIds.contains(parentId)) {
                roots.add(item);
                continue;
            }

            MenuTreeItem parent = menuMap.get(parentId);
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(item);
        }

        sortMenuItems(roots);
        return roots;
    }

    private List<MenuTreeItem> filterMenuTree(List<MenuTreeItem> items, Map<String, String> params) {
        String menuName = defaultIfBlank(params.get("menuName"), "").toLowerCase();
        String path = defaultIfBlank(params.get("path"), "").toLowerCase();
        String perms = defaultIfBlank(params.get("perms"), "").toLowerCase();
        String menuType = defaultIfBlank(params.get("menuType"), "");
        String enabled = params.get("enabled");

        if (!hasText(menuName) && !hasText(path) && !hasText(perms) && !hasText(menuType) && !hasText(enabled)) {
            return items;
        }

        List<MenuTreeItem> results = new ArrayList<>();
        for (MenuTreeItem item : items) {
            List<MenuTreeItem> matchedChildren = item.getChildren() == null
                    ? List.of()
                    : filterMenuTree(item.getChildren(), params);
            boolean selfMatched = menuMatches(item, menuName, path, perms, menuType, enabled);
            if (selfMatched || !matchedChildren.isEmpty()) {
                item.setChildren(matchedChildren.isEmpty() ? null : matchedChildren);
                results.add(item);
            }
        }
        return results;
    }

    private boolean menuMatches(MenuTreeItem item, String menuName, String path, String perms, String menuType, String enabled) {
        return (!hasText(menuName) || defaultIfBlank(item.getMenuName(), "").toLowerCase().contains(menuName))
                && (!hasText(path) || defaultIfBlank(item.getPath(), "").toLowerCase().contains(path))
                && (!hasText(perms) || defaultIfBlank(item.getPerms(), "").toLowerCase().contains(perms))
                && (!hasText(menuType) || Objects.equals(item.getMenuType(), normalizeMenuType(menuType)))
                && (!hasText(enabled) || Objects.equals(item.getEnabled(), Boolean.parseBoolean(enabled)));
    }

    private void sortMenuItems(List<MenuTreeItem> items) {
        items.sort(Comparator.comparing((MenuTreeItem item) -> item.getOrderNum() == null ? 0 : item.getOrderNum())
                .thenComparing(MenuTreeItem::getMenuId));
        for (MenuTreeItem item : items) {
            if (item.getChildren() != null) {
                sortMenuItems(item.getChildren());
            }
        }
    }

    private List<AppRouteRecord> buildRouteTree(List<SysMenu> menus) {
        Map<Long, AppRouteRecord> routeMap = menus.stream()
                .sorted(Comparator.comparing(SysMenu::getOrderNum).thenComparing(SysMenu::getMenuId))
                .map(this::toRouteRecord)
                .collect(Collectors.toMap(AppRouteRecord::getId, Function.identity(), (left, right) -> left));

        List<AppRouteRecord> roots = new ArrayList<>();
        Set<Long> validIds = routeMap.keySet();
        for (SysMenu menu : menus) {
            AppRouteRecord route = routeMap.get(menu.getMenuId());
            Long parentId = menu.getParentId();
            if (parentId == null || parentId == 0 || !validIds.contains(parentId)) {
                roots.add(route);
                continue;
            }

            AppRouteRecord parent = routeMap.get(parentId);
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(route);
        }

        sortRoutes(roots);
        return roots;
    }

    private AppRouteRecord toRouteRecord(SysMenu menu) {
        RouteMeta meta = RouteMeta.of(menu.getMenuName(), menu.getIcon());
        meta.setKeepAlive(Objects.equals(menu.getIsCache(), "0"));
        meta.setIsHide(Objects.equals(menu.getVisible(), "2"));

        return AppRouteRecord.route(
                menu.getMenuId(),
                menu.getPath(),
                menu.getRouteName(),
                menu.getComponent(),
                meta
        );
    }

    private void sortRoutes(List<AppRouteRecord> routes) {
        routes.sort(Comparator.comparing(AppRouteRecord::getId));
        for (AppRouteRecord route : routes) {
            if (route.getChildren() != null) {
                sortRoutes(route.getChildren());
            }
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    private String booleanToStatus(Boolean enabled) {
        return Boolean.FALSE.equals(enabled) ? "2" : "1";
    }

    private String normalizeMenuType(String menuType) {
        if (Objects.equals(menuType, "directory") || Objects.equals(menuType, "M")) {
            return "M";
        }
        if (Objects.equals(menuType, "button") || Objects.equals(menuType, "F")) {
            return "F";
        }
        return "C";
    }

    private String deriveRouteName(String path, String menuName) {
        String source = hasText(path) ? path : menuName;
        String[] parts = source.replace("/", "-").split("-");
        StringBuilder name = new StringBuilder();
        for (String part : parts) {
            if (!hasText(part)) {
                continue;
            }
            name.append(part.substring(0, 1).toUpperCase());
            if (part.length() > 1) {
                name.append(part.substring(1));
            }
        }
        return name.isEmpty() ? "Menu" : name.toString();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }
}

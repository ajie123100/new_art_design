package com.artdesign.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.annotation.Log;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.enums.BusinessType;
import com.artdesign.system.domain.dto.MenuSaveRequest;
import com.artdesign.system.domain.dto.MenuStatusRequest;
import com.artdesign.system.domain.dto.MenuTreeItem;
import com.artdesign.system.service.SysMenuService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
public class SysMenuManageController {
    private final SysMenuService menuService;

    public SysMenuManageController(SysMenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/list")
    @SaCheckPermission("system:menu:list")
    public R<List<MenuTreeItem>> list(@RequestParam Map<String, String> params) {
        return R.ok(menuService.listSystemMenus(params));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:menu:list")
    public R<MenuTreeItem> get(@PathVariable Long id) {
        return R.ok(menuService.getSystemMenu(id));
    }

    @PostMapping
    @SaCheckPermission("system:menu:add")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    public R<Long> create(@Valid @RequestBody MenuSaveRequest request) {
        return R.ok(menuService.createMenu(request));
    }

    @PutMapping
    @SaCheckPermission("system:menu:edit")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    public R<Void> update(@Valid @RequestBody MenuSaveRequest request) {
        menuService.updateMenu(request);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    @SaCheckPermission("system:menu:delete")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    public R<Void> delete(@PathVariable String ids) {
        List<Long> menuIds = Arrays.stream(ids.split(","))
                .filter(id -> !id.isBlank())
                .map(Long::valueOf)
                .toList();
        menuService.deleteMenus(menuIds);
        return R.ok();
    }

    @PutMapping("/status")
    @SaCheckPermission("system:menu:edit")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    public R<Void> updateStatus(@Valid @RequestBody MenuStatusRequest request) {
        menuService.updateMenuStatus(request);
        return R.ok();
    }
}

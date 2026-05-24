package com.artdesign.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.annotation.Log;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.enums.BusinessType;
import com.artdesign.common.utils.ExcelUtil;
import com.artdesign.system.domain.dto.AppRouteRecord;
import com.artdesign.system.domain.dto.ImportResult;
import com.artdesign.system.domain.dto.RoleExcel;
import com.artdesign.system.domain.dto.RoleMenuRequest;
import com.artdesign.system.domain.dto.RoleListItem;
import com.artdesign.system.domain.dto.RoleSaveRequest;
import com.artdesign.system.domain.dto.RoleStatusRequest;
import com.artdesign.system.service.SysRoleService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role")
public class SysRoleController {
    private final SysRoleService roleService;

    public SysRoleController(SysRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/list")
    @SaCheckPermission("system:role:list")
    public R<PageResult<RoleListItem>> list(@RequestParam Map<String, String> params) {
        return R.ok(roleService.listRoles(params));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:role:list")
    public R<RoleListItem> get(@PathVariable Long id) {
        return R.ok(roleService.getRole(id));
    }

    @GetMapping("/export")
    @SaCheckPermission("system:role:export")
    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    public void export(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        ExcelUtil.writeExcel(response, roleService.exportRoles(params), RoleExcel.class, "role");
    }

    @PostMapping("/import")
    @SaCheckPermission("system:role:import")
    @Log(title = "角色管理", businessType = BusinessType.IMPORT)
    public R<ImportResult> importRoles(@RequestPart("file") Part file) throws IOException {
        return R.ok(roleService.importRoles(ExcelUtil.readExcel(file, RoleExcel.class)));
    }

    @PostMapping
    @SaCheckPermission("system:role:add")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    public R<Long> create(@Valid @RequestBody RoleSaveRequest request) {
        return R.ok(roleService.createRole(request));
    }

    @PutMapping
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    public R<Void> update(@Valid @RequestBody RoleSaveRequest request) {
        roleService.updateRole(request);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    @SaCheckPermission("system:role:delete")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    public R<Void> delete(@PathVariable String ids) {
        List<Long> roleIds = Arrays.stream(ids.split(","))
                .filter(id -> !id.isBlank())
                .map(Long::valueOf)
                .toList();
        roleService.deleteRoles(roleIds);
        return R.ok();
    }

    @PutMapping("/status")
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    public R<Void> updateStatus(@Valid @RequestBody RoleStatusRequest request) {
        roleService.updateRoleStatus(request);
        return R.ok();
    }

    @GetMapping("/menu-tree")
    @SaCheckPermission("system:role:grant")
    public R<List<AppRouteRecord>> menuTree() {
        return R.ok(roleService.getGrantMenuTree());
    }

    @GetMapping("/{id}/menu-ids")
    @SaCheckPermission("system:role:grant")
    public R<List<Long>> menuIds(@PathVariable Long id) {
        return R.ok(roleService.getRoleMenuIds(id));
    }

    @GetMapping("/{id}/dept-ids")
    @SaCheckPermission("system:role:list")
    public R<List<Long>> deptIds(@PathVariable Long id) {
        return R.ok(roleService.getRoleDeptIds(id));
    }

    @PutMapping("/menu")
    @SaCheckPermission("system:role:grant")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    public R<Void> saveMenus(@Valid @RequestBody RoleMenuRequest request) {
        roleService.saveRoleMenus(request);
        return R.ok();
    }
}

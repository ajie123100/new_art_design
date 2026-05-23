package com.artdesign.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.annotation.Log;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.enums.BusinessType;
import com.artdesign.system.domain.dto.DeptSaveRequest;
import com.artdesign.system.domain.dto.DeptStatusRequest;
import com.artdesign.system.domain.dto.DeptTreeItem;
import com.artdesign.system.service.SysDeptService;
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
@RequestMapping("/api/dept")
public class SysDeptController {
    private final SysDeptService deptService;

    public SysDeptController(SysDeptService deptService) {
        this.deptService = deptService;
    }

    @GetMapping("/list")
    @SaCheckPermission("system:dept:list")
    public R<List<DeptTreeItem>> list(@RequestParam Map<String, String> params) {
        return R.ok(deptService.listDepartments(params));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:dept:list")
    public R<DeptTreeItem> get(@PathVariable Long id) {
        return R.ok(deptService.getDepartment(id));
    }

    @PostMapping
    @SaCheckPermission("system:dept:add")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    public R<Long> create(@Valid @RequestBody DeptSaveRequest request) {
        return R.ok(deptService.createDepartment(request));
    }

    @PutMapping
    @SaCheckPermission("system:dept:edit")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    public R<Void> update(@Valid @RequestBody DeptSaveRequest request) {
        deptService.updateDepartment(request);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    @SaCheckPermission("system:dept:delete")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    public R<Void> delete(@PathVariable String ids) {
        List<Long> deptIds = Arrays.stream(ids.split(","))
                .filter(id -> !id.isBlank())
                .map(Long::valueOf)
                .toList();
        deptService.deleteDepartments(deptIds);
        return R.ok();
    }

    @PutMapping("/status")
    @SaCheckPermission("system:dept:edit")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    public R<Void> updateStatus(@Valid @RequestBody DeptStatusRequest request) {
        deptService.updateStatus(request);
        return R.ok();
    }
}

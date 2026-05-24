package com.artdesign.web.controller.system;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.annotation.Log;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.enums.BusinessType;
import com.artdesign.common.utils.ExcelUtil;
import com.artdesign.system.domain.dto.ImportResult;
import com.artdesign.system.domain.dto.UserExcel;
import com.artdesign.system.domain.dto.UserResetPasswordRequest;
import com.artdesign.system.domain.dto.UserSaveRequest;
import com.artdesign.system.domain.dto.UserInfo;
import com.artdesign.system.domain.dto.UserListItem;
import com.artdesign.system.domain.dto.UserStatusRequest;
import com.artdesign.system.service.SysAuthService;
import com.artdesign.system.service.SysUserService;
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
@RequestMapping("/api/user")
public class SysUserController {
    private final SysAuthService authService;
    private final SysUserService userService;

    public SysUserController(SysAuthService authService, SysUserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @GetMapping("/info")
    public R<UserInfo> info() {
        return R.ok(authService.getUserInfo(StpUtil.getLoginIdAsLong()));
    }

    @GetMapping("/list")
    @SaCheckPermission("system:user:list")
    public R<PageResult<UserListItem>> list(@RequestParam Map<String, String> params) {
        return R.ok(userService.listUsers(params));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:user:list")
    public R<UserListItem> get(@PathVariable Long id) {
        return R.ok(userService.getUser(id));
    }

    @GetMapping("/export")
    @SaCheckPermission("system:user:export")
    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    public void export(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        ExcelUtil.writeExcel(response, userService.exportUsers(params), UserExcel.class, "user");
    }

    @PostMapping("/import")
    @SaCheckPermission("system:user:import")
    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    public R<ImportResult> importUsers(@RequestPart("file") Part file) throws IOException {
        return R.ok(userService.importUsers(ExcelUtil.readExcel(file, UserExcel.class)));
    }

    @PostMapping
    @SaCheckPermission("system:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    public R<Long> create(@Valid @RequestBody UserSaveRequest request) {
        return R.ok(userService.createUser(request));
    }

    @PutMapping
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    public R<Void> update(@Valid @RequestBody UserSaveRequest request) {
        userService.updateUser(request);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    @SaCheckPermission("system:user:delete")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    public R<Void> delete(@PathVariable String ids) {
        List<Long> userIds = Arrays.stream(ids.split(","))
                .filter(id -> !id.isBlank())
                .map(Long::valueOf)
                .toList();
        userService.deleteUsers(userIds);
        return R.ok();
    }

    @PutMapping("/status")
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    public R<Void> updateStatus(@Valid @RequestBody UserStatusRequest request) {
        userService.updateStatus(request);
        return R.ok();
    }

    @PutMapping("/reset-password")
    @SaCheckPermission("system:user:resetPwd")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE, saveRequestData = false)
    public R<Void> resetPassword(@Valid @RequestBody UserResetPasswordRequest request) {
        userService.resetPassword(request);
        return R.ok();
    }
}

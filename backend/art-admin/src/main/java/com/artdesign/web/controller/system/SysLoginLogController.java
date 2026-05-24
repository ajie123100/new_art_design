package com.artdesign.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.annotation.Log;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.enums.BusinessType;
import com.artdesign.common.utils.ExcelUtil;
import com.artdesign.system.domain.dto.LoginLogListItem;
import com.artdesign.system.service.SysLoginLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/logininfor")
public class SysLoginLogController {
    private final SysLoginLogService loginLogService;

    public SysLoginLogController(SysLoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    @GetMapping("/list")
    @SaCheckPermission("monitor:logininfor:list")
    public R<PageResult<LoginLogListItem>> list(@RequestParam Map<String, String> params) {
        return R.ok(loginLogService.list(params));
    }

    @GetMapping("/export")
    @SaCheckPermission("monitor:logininfor:export")
    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    public void export(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        ExcelUtil.writeExcel(response, loginLogService.exportList(params), LoginLogListItem.class, "login_log");
    }

    @DeleteMapping("/clean")
    @SaCheckPermission("monitor:logininfor:delete")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    public R<Void> clean() {
        loginLogService.clean();
        return R.ok();
    }
}

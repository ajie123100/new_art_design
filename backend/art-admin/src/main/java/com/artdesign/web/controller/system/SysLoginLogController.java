package com.artdesign.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.core.page.PageResult;
import com.artdesign.system.domain.dto.LoginLogListItem;
import com.artdesign.system.service.SysLoginLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}

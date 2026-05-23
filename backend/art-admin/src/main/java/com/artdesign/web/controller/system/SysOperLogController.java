package com.artdesign.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.core.page.PageResult;
import com.artdesign.system.domain.dto.OperLogListItem;
import com.artdesign.system.service.SysOperLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/operlog")
public class SysOperLogController {
    private final SysOperLogService operLogService;

    public SysOperLogController(SysOperLogService operLogService) {
        this.operLogService = operLogService;
    }

    @GetMapping("/list")
    @SaCheckPermission("monitor:operlog:list")
    public R<PageResult<OperLogListItem>> list(@RequestParam Map<String, String> params) {
        return R.ok(operLogService.list(params));
    }
}

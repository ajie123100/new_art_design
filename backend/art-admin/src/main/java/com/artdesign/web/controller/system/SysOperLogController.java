package com.artdesign.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.annotation.Log;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.enums.BusinessType;
import com.artdesign.common.utils.ExcelUtil;
import com.artdesign.system.domain.dto.OperLogListItem;
import com.artdesign.system.service.SysOperLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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

    @GetMapping("/export")
    @SaCheckPermission("monitor:operlog:export")
    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    public void export(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        ExcelUtil.writeExcel(response, operLogService.exportList(params), OperLogListItem.class, "oper_log");
    }

    @DeleteMapping("/clean")
    @SaCheckPermission("monitor:operlog:delete")
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    public R<Void> clean() {
        operLogService.clean();
        return R.ok();
    }
}

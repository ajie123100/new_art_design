package com.artdesign.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.annotation.Log;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.enums.BusinessType;
import com.artdesign.common.utils.ExcelUtil;
import com.artdesign.system.domain.dto.JobLogListItem;
import com.artdesign.system.service.SysJobLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;

@RestController
@RequestMapping("/api/jobLog")
public class SysJobLogController {
    private final SysJobLogService jobLogService;

    public SysJobLogController(SysJobLogService jobLogService) {
        this.jobLogService = jobLogService;
    }

    @GetMapping("/list")
    @SaCheckPermission("monitor:job:list")
    public R<PageResult<JobLogListItem>> list(@RequestParam Map<String, String> params) {
        return R.ok(jobLogService.list(params));
    }

    @GetMapping("/export")
    @SaCheckPermission("monitor:job:export")
    @Log(title = "任务日志", businessType = BusinessType.EXPORT)
    public void export(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        ExcelUtil.writeExcel(response, jobLogService.exportList(params), JobLogListItem.class, "job_log");
    }

    @DeleteMapping("/{ids}")
    @SaCheckPermission("monitor:job:delete")
    @Log(title = "任务日志", businessType = BusinessType.DELETE)
    public R<Void> delete(@PathVariable String ids) {
        List<Long> jobLogIds = Arrays.stream(ids.split(","))
                .filter(id -> !id.isBlank())
                .map(Long::valueOf)
                .toList();
        jobLogService.delete(jobLogIds);
        return R.ok();
    }

    @DeleteMapping("/clean")
    @SaCheckPermission("monitor:job:delete")
    @Log(title = "任务日志", businessType = BusinessType.CLEAN)
    public R<Void> clean() {
        jobLogService.clean();
        return R.ok();
    }
}

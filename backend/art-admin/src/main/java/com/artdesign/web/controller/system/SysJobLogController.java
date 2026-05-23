package com.artdesign.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.core.page.PageResult;
import com.artdesign.system.domain.dto.JobLogListItem;
import com.artdesign.system.service.SysJobLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
}

package com.artdesign.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.annotation.Log;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.enums.BusinessType;
import com.artdesign.system.domain.dto.JobListItem;
import com.artdesign.system.domain.dto.JobSaveRequest;
import com.artdesign.system.service.SysJobService;
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
@RequestMapping("/api/job")
public class SysJobController {
    private final SysJobService jobService;

    public SysJobController(SysJobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/list")
    @SaCheckPermission("monitor:job:list")
    public R<PageResult<JobListItem>> list(@RequestParam Map<String, String> params) {
        return R.ok(jobService.list(params));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("monitor:job:list")
    public R<JobListItem> get(@PathVariable Long id) {
        return R.ok(jobService.get(id));
    }

    @PostMapping
    @SaCheckPermission("monitor:job:add")
    @Log(title = "定时任务", businessType = BusinessType.INSERT)
    public R<Long> create(@Valid @RequestBody JobSaveRequest request) {
        return R.ok(jobService.create(request));
    }

    @PutMapping
    @SaCheckPermission("monitor:job:edit")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    public R<Void> update(@Valid @RequestBody JobSaveRequest request) {
        jobService.update(request);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    @SaCheckPermission("monitor:job:delete")
    @Log(title = "定时任务", businessType = BusinessType.DELETE)
    public R<Void> delete(@PathVariable String ids) {
        List<Long> jobIds = Arrays.stream(ids.split(",")).filter(id -> !id.isBlank()).map(Long::valueOf).toList();
        jobService.delete(jobIds);
        return R.ok();
    }

    @PutMapping("/changeStatus/{jobId}/{status}")
    @SaCheckPermission("monitor:job:edit")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    public R<Void> changeStatus(@PathVariable Long jobId, @PathVariable String status) {
        jobService.changeStatus(jobId, status);
        return R.ok();
    }

    @PostMapping("/run/{jobIds}")
    @SaCheckPermission("monitor:job:edit")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    public R<Void> run(@PathVariable String jobIds) {
        List<Long> ids = Arrays.stream(jobIds.split(",")).filter(id -> !id.isBlank()).map(Long::valueOf).toList();
        jobService.run(ids);
        return R.ok();
    }
}

package com.artdesign.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.annotation.Log;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.enums.BusinessType;
import com.artdesign.system.domain.dto.NoticeListItem;
import com.artdesign.system.domain.dto.NoticeSaveRequest;
import com.artdesign.system.domain.entity.SysNotice;
import com.artdesign.system.service.SysNoticeService;
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
@RequestMapping("/api/notice")
public class SysNoticeController {
    private final SysNoticeService noticeService;

    public SysNoticeController(SysNoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/list")
    @SaCheckPermission("system:notice:list")
    public R<PageResult<NoticeListItem>> list(@RequestParam Map<String, String> params) {
        return R.ok(noticeService.list(params));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:notice:list")
    public R<SysNotice> get(@PathVariable Long id) {
        return R.ok(noticeService.get(id));
    }

    @PostMapping
    @SaCheckPermission("system:notice:add")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    public R<Long> create(@Valid @RequestBody NoticeSaveRequest request) {
        return R.ok(noticeService.create(request));
    }

    @PutMapping
    @SaCheckPermission("system:notice:edit")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    public R<Void> update(@Valid @RequestBody NoticeSaveRequest request) {
        noticeService.update(request);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    @SaCheckPermission("system:notice:delete")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    public R<Void> delete(@PathVariable String ids) {
        List<Long> noticeIds = Arrays.stream(ids.split(","))
                .filter(id -> !id.isBlank())
                .map(Long::valueOf)
                .toList();
        noticeService.delete(noticeIds);
        return R.ok();
    }
}

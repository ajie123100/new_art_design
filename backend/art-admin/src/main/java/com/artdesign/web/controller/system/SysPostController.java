package com.artdesign.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.annotation.Log;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.enums.BusinessType;
import com.artdesign.system.domain.dto.PostListItem;
import com.artdesign.system.domain.dto.PostSaveRequest;
import com.artdesign.system.service.SysPostService;
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
@RequestMapping("/api/post")
public class SysPostController {
    private final SysPostService postService;

    public SysPostController(SysPostService postService) {
        this.postService = postService;
    }

    @GetMapping("/list")
    @SaCheckPermission("system:post:list")
    public R<PageResult<PostListItem>> list(@RequestParam Map<String, String> params) {
        return R.ok(postService.list(params));
    }

    @GetMapping("/listAll")
    public R<List<PostListItem>> listAll() {
        return R.ok(postService.listAll());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:post:list")
    public R<PostListItem> get(@PathVariable Long id) {
        return R.ok(postService.get(id));
    }

    @PostMapping
    @SaCheckPermission("system:post:add")
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    public R<Long> create(@Valid @RequestBody PostSaveRequest request) {
        return R.ok(postService.create(request));
    }

    @PutMapping
    @SaCheckPermission("system:post:edit")
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    public R<Void> update(@Valid @RequestBody PostSaveRequest request) {
        postService.update(request);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    @SaCheckPermission("system:post:delete")
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    public R<Void> delete(@PathVariable String ids) {
        List<Long> postIds = Arrays.stream(ids.split(","))
                .filter(id -> !id.isBlank())
                .map(Long::valueOf)
                .toList();
        postService.delete(postIds);
        return R.ok();
    }
}

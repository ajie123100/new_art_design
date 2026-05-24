package com.artdesign.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.annotation.Log;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.enums.BusinessType;
import com.artdesign.common.utils.ExcelUtil;
import com.artdesign.system.domain.dto.ConfigExcel;
import com.artdesign.system.domain.dto.ConfigListItem;
import com.artdesign.system.domain.dto.ConfigSaveRequest;
import com.artdesign.system.domain.dto.ImportResult;
import com.artdesign.system.service.SysConfigService;
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
@RequestMapping("/api/config")
public class SysConfigController {
    private final SysConfigService configService;

    public SysConfigController(SysConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/list")
    @SaCheckPermission("system:config:list")
    public R<PageResult<ConfigListItem>> list(@RequestParam Map<String, String> params) {
        return R.ok(configService.list(params));
    }

    @GetMapping("/key/{configKey}")
    public R<String> getConfigKey(@PathVariable String configKey) {
        return R.ok(configService.getConfigKey(configKey));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:config:list")
    public R<ConfigListItem> get(@PathVariable Long id) {
        return R.ok(configService.get(id));
    }

    @GetMapping("/export")
    @SaCheckPermission("system:config:export")
    @Log(title = "参数管理", businessType = BusinessType.EXPORT)
    public void export(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        ExcelUtil.writeExcel(response, configService.exportConfigs(params), ConfigExcel.class, "config");
    }

    @PostMapping("/import")
    @SaCheckPermission("system:config:import")
    @Log(title = "参数管理", businessType = BusinessType.IMPORT)
    public R<ImportResult> importConfigs(@RequestPart("file") Part file) throws IOException {
        return R.ok(configService.importConfigs(ExcelUtil.readExcel(file, ConfigExcel.class)));
    }

    @PostMapping
    @SaCheckPermission("system:config:add")
    @Log(title = "参数管理", businessType = BusinessType.INSERT)
    public R<Long> create(@Valid @RequestBody ConfigSaveRequest request) {
        return R.ok(configService.create(request));
    }

    @PutMapping
    @SaCheckPermission("system:config:edit")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    public R<Void> update(@Valid @RequestBody ConfigSaveRequest request) {
        configService.update(request);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    @SaCheckPermission("system:config:delete")
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    public R<Void> delete(@PathVariable String ids) {
        List<Long> configIds = Arrays.stream(ids.split(","))
                .filter(id -> !id.isBlank())
                .map(Long::valueOf)
                .toList();
        configService.delete(configIds);
        return R.ok();
    }

    @DeleteMapping("/refreshCache")
    @SaCheckPermission("system:config:edit")
    @Log(title = "参数管理", businessType = BusinessType.CLEAN)
    public R<Void> refreshCache() {
        configService.refreshConfigCache();
        return R.ok();
    }
}

package com.artdesign.web.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.annotation.Log;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.enums.BusinessType;
import com.artdesign.system.domain.dto.CacheInfo;
import com.artdesign.system.domain.dto.OnlineUser;
import com.artdesign.system.domain.dto.ServerInfo;
import com.artdesign.system.service.SysCacheService;
import com.artdesign.system.service.SysOnlineUserService;
import com.artdesign.system.service.SysServerService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
public class SysMonitorController {

    private final SysServerService serverService;
    private final SysOnlineUserService onlineUserService;
    private final SysCacheService cacheService;

    public SysMonitorController(SysServerService serverService, SysOnlineUserService onlineUserService, SysCacheService cacheService) {
        this.serverService = serverService;
        this.onlineUserService = onlineUserService;
        this.cacheService = cacheService;
    }

    @GetMapping("/server")
    @SaCheckPermission("monitor:server:view")
    public R<ServerInfo> server() {
        return R.ok(serverService.getInfo());
    }

    @GetMapping("/online/list")
    @SaCheckPermission("monitor:online:list")
    public R<List<OnlineUser>> online() {
        return R.ok(onlineUserService.list());
    }

    @DeleteMapping("/online/{loginId}")
    @SaCheckPermission("monitor:online:forceLogout")
    @Log(title = "在线用户", businessType = BusinessType.DELETE)
    public R<Void> forceLogout(@PathVariable Long loginId) {
        onlineUserService.forceLogout(loginId);
        return R.ok();
    }

    @GetMapping("/cache")
    @SaCheckPermission("monitor:cache:view")
    public R<CacheInfo> cache() {
        return R.ok(cacheService.getInfo());
    }

    @DeleteMapping("/cache/{key}")
    @SaCheckPermission("monitor:cache:edit")
    @Log(title = "缓存监控", businessType = BusinessType.DELETE)
    public R<Void> clearCache(@PathVariable String key) {
        cacheService.clear(key);
        return R.ok();
    }
}

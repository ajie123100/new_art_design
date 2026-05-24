package com.artdesign.web.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.artdesign.common.annotation.Log;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.enums.BusinessType;
import com.artdesign.system.domain.dto.CacheInfo;
import com.artdesign.system.domain.dto.CacheNameInfo;
import com.artdesign.system.domain.dto.DatabaseInfo;
import com.artdesign.system.domain.dto.OnlineUser;
import com.artdesign.system.domain.dto.ServerInfo;
import com.artdesign.system.service.SysCacheService;
import com.artdesign.system.service.SysDatabaseService;
import com.artdesign.system.service.SysOnlineUserService;
import com.artdesign.system.service.SysServerService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
public class SysMonitorController {

    private final SysServerService serverService;
    private final SysOnlineUserService onlineUserService;
    private final SysCacheService cacheService;
    private final SysDatabaseService databaseService;

    public SysMonitorController(SysServerService serverService, SysOnlineUserService onlineUserService, SysCacheService cacheService, SysDatabaseService databaseService) {
        this.serverService = serverService;
        this.onlineUserService = onlineUserService;
        this.cacheService = cacheService;
        this.databaseService = databaseService;
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

    @DeleteMapping("/online/{tokenValue}")
    @SaCheckPermission("monitor:online:forceLogout")
    @Log(title = "在线用户", businessType = BusinessType.DELETE)
    public R<Void> forceLogout(@PathVariable String tokenValue) {
        onlineUserService.forceLogout(tokenValue);
        return R.ok();
    }

    @GetMapping("/cache")
    @SaCheckPermission("monitor:cache:view")
    public R<CacheInfo> cache() {
        return R.ok(cacheService.getInfo());
    }

    @GetMapping("/database")
    @SaCheckPermission("monitor:database:view")
    public R<DatabaseInfo> database() {
        return R.ok(databaseService.getInfo());
    }

    @GetMapping("/cache/names")
    @SaCheckPermission("monitor:cache:view")
    public R<List<CacheNameInfo>> cacheNames() {
        return R.ok(cacheService.cacheNames());
    }

    @GetMapping("/cache/keys/{cacheName}")
    @SaCheckPermission("monitor:cache:view")
    public R<List<String>> cacheKeys(@PathVariable String cacheName) {
        return R.ok(cacheService.keys(cacheName));
    }

    @GetMapping("/cache/value/{cacheName}")
    @SaCheckPermission("monitor:cache:view")
    public R<Object> cacheValue(@PathVariable String cacheName, @RequestParam String key) {
        return R.ok(cacheService.value(cacheName, key));
    }

    @DeleteMapping("/cache/{key}")
    @SaCheckPermission("monitor:cache:edit")
    @Log(title = "缓存监控", businessType = BusinessType.DELETE)
    public R<Void> clearCache(@PathVariable String key) {
        cacheService.clear(key);
        return R.ok();
    }

    @DeleteMapping("/cache/{cacheName}/keys")
    @SaCheckPermission("monitor:cache:edit")
    @Log(title = "缓存监控", businessType = BusinessType.DELETE)
    public R<Void> clearCacheKey(@PathVariable String cacheName, @RequestParam String key) {
        cacheService.clear(cacheName, key);
        return R.ok();
    }

    @DeleteMapping("/cache/name/{cacheName}")
    @SaCheckPermission("monitor:cache:edit")
    @Log(title = "缓存监控", businessType = BusinessType.CLEAN)
    public R<Void> clearCacheName(@PathVariable String cacheName) {
        cacheService.clearCacheName(cacheName);
        return R.ok();
    }
}

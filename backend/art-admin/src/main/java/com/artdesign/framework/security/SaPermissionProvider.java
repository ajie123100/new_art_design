package com.artdesign.framework.security;

import cn.dev33.satoken.stp.StpInterface;
import com.artdesign.system.service.SysAuthService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SaPermissionProvider implements StpInterface {
    private final SysAuthService authService;

    public SaPermissionProvider(SysAuthService authService) {
        this.authService = authService;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return authService.getPermissionList(Long.valueOf(loginId.toString()));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return authService.getRoleList(Long.valueOf(loginId.toString()));
    }
}

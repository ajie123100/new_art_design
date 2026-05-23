package com.artdesign.web.controller.auth;

import cn.dev33.satoken.stp.StpUtil;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.utils.ServletUtils;
import com.artdesign.system.domain.dto.LoginResponse;
import com.artdesign.system.domain.entity.SysUser;
import com.artdesign.system.service.SysAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final SysAuthService authService;

    public AuthController(SysAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipaddr = ServletUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        SysUser user = authService.login(request.userName(), request.password(), ipaddr, userAgent);
        StpUtil.login(user.getUserId());
        String token = StpUtil.getTokenValue();
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        return R.ok(new LoginResponse(token, refreshToken));
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        StpUtil.logout();
        return R.ok();
    }

    public record LoginRequest(
            @NotBlank String userName,
            @NotBlank String password
    ) {
    }
}

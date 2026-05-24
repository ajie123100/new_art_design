package com.artdesign.web.controller.auth;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.SaLoginModel;
import com.artdesign.common.core.domain.R;
import com.artdesign.common.utils.ServletUtils;
import com.artdesign.system.domain.dto.CaptchaResponse;
import com.artdesign.system.domain.dto.LoginResponse;
import com.artdesign.system.domain.entity.SysUser;
import com.artdesign.system.service.SysAuthService;
import com.artdesign.system.service.SysCaptchaService;
import com.artdesign.system.service.SysRefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final SysAuthService authService;
    private final SysCaptchaService captchaService;
    private final SysRefreshTokenService refreshTokenService;
    private final long accessTokenTimeout;

    public AuthController(
            SysAuthService authService,
            SysCaptchaService captchaService,
            SysRefreshTokenService refreshTokenService,
            @Value("${art.auth.access-token-timeout:1800}") long accessTokenTimeout) {
        this.authService = authService;
        this.captchaService = captchaService;
        this.refreshTokenService = refreshTokenService;
        this.accessTokenTimeout = accessTokenTimeout;
    }

    @GetMapping("/captchaImage")
    public R<CaptchaResponse> captchaImage() {
        return R.ok(captchaService.createCaptcha());
    }

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        captchaService.validate(request.uuid(), request.code());
        String ipaddr = ServletUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        SysUser user = authService.login(request.userName(), request.password(), ipaddr, userAgent);
        return R.ok(createLoginResponse(user.getUserId(), null, ipaddr, userAgent, request.deviceId()));
    }

    @PostMapping("/refresh")
    public R<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        SysRefreshTokenService.RefreshTokenRecord record = refreshTokenService.resolve(request.refreshToken());
        Long userId = record.userId();
        authService.getActiveUser(userId);
        String ipaddr = ServletUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String deviceId = request.deviceId() == null ? record.deviceId() : request.deviceId();
        return R.ok(createLoginResponse(userId, request.refreshToken(), ipaddr, userAgent, deviceId));
    }

    @PostMapping("/logout")
    public R<Void> logout(@RequestBody(required = false) LogoutRequest request) {
        if (request != null) {
            refreshTokenService.revoke(request.refreshToken());
        }
        StpUtil.logout();
        return R.ok();
    }

    private LoginResponse createLoginResponse(Long userId, String oldRefreshToken, String ipaddr, String userAgent, String deviceId) {
        StpUtil.login(userId, new SaLoginModel()
                .setTimeout(accessTokenTimeout)
                .setDevice(deviceId == null || deviceId.isBlank() ? "default" : deviceId));
        fillTokenSession(ipaddr, userAgent, deviceId);
        String token = StpUtil.getTokenValue();
        String refreshToken = oldRefreshToken == null
                ? refreshTokenService.issue(userId, ipaddr, userAgent, deviceId)
                : refreshTokenService.rotate(oldRefreshToken, userId, ipaddr, userAgent, deviceId);
        return new LoginResponse(token, refreshToken, accessTokenTimeout);
    }

    private void fillTokenSession(String ipaddr, String userAgent, String deviceId) {
        var tokenSession = StpUtil.getTokenSession();
        tokenSession.set("ipaddr", ipaddr == null ? "" : ipaddr);
        tokenSession.set("loginLocation", "");
        tokenSession.set("browser", resolveBrowser(userAgent));
        tokenSession.set("os", resolveOs(userAgent));
        tokenSession.set("loginTime", LocalDateTime.now());
        tokenSession.set("deviceId", deviceId == null ? "" : deviceId);
    }

    private String resolveBrowser(String userAgent) {
        if (userAgent == null) {
            return "";
        }
        if (userAgent.contains("Edg/")) return "Edge";
        if (userAgent.contains("Chrome/")) return "Chrome";
        if (userAgent.contains("Firefox/")) return "Firefox";
        if (userAgent.contains("Safari/")) return "Safari";
        return "Unknown";
    }

    private String resolveOs(String userAgent) {
        if (userAgent == null) {
            return "";
        }
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac OS")) return "macOS";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        if (userAgent.contains("Linux")) return "Linux";
        return "Unknown";
    }

    public record LoginRequest(
            @NotBlank String userName,
            @NotBlank String password,
            String code,
            String uuid,
            String deviceId
    ) {
    }

    public record RefreshTokenRequest(
            @NotBlank String refreshToken,
            String deviceId
    ) {
    }

    public record LogoutRequest(
            String refreshToken
    ) {
    }
}

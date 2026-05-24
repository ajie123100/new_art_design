package com.artdesign.system.service;

import com.artdesign.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class SysRefreshTokenService {
    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh:";
    private static final String REFRESH_TOKEN_REUSED_KEY_PREFIX = "auth:refresh:reused:";
    private static final Duration REUSED_TOKEN_TTL = Duration.ofDays(1);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RedisTemplate<String, Object> redisTemplate;
    private final long refreshTokenTimeout;

    public SysRefreshTokenService(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${art.auth.refresh-token-timeout:604800}") long refreshTokenTimeout) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenTimeout = refreshTokenTimeout;
    }

    public String issue(Long userId) {
        return issue(userId, "", "");
    }

    public String issue(Long userId, String ipaddr, String userAgent) {
        return issue(userId, ipaddr, userAgent, "");
    }

    public String issue(Long userId, String ipaddr, String userAgent, String deviceId) {
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        String now = DATE_TIME.format(LocalDateTime.now());
        RefreshTokenRecord record = new RefreshTokenRecord(
                userId,
                nullToEmpty(ipaddr),
                nullToEmpty(userAgent),
                nullToEmpty(deviceId),
                now,
                now
        );
        redisTemplate.opsForValue().set(tokenKey(refreshToken), record, Duration.ofSeconds(refreshTokenTimeout));
        return refreshToken;
    }

    public Long resolveUserId(String refreshToken) {
        return resolve(refreshToken).userId();
    }

    public RefreshTokenRecord resolve(String refreshToken) {
        if (!hasText(refreshToken)) {
            throw new BusinessException("刷新令牌不能为空");
        }
        String reusedKey = reusedTokenKey(refreshToken);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(reusedKey))) {
            redisTemplate.delete(reusedKey);
            throw new BusinessException("刷新令牌已轮换，请重新登录");
        }
        Object userId = redisTemplate.opsForValue().get(tokenKey(refreshToken));
        if (userId == null) {
            throw new BusinessException("刷新令牌已失效");
        }
        if (userId instanceof RefreshTokenRecord record) {
            return record;
        }
        try {
            return new RefreshTokenRecord(Long.valueOf(userId.toString()), "", "", "", "", "");
        } catch (NumberFormatException ex) {
            revoke(refreshToken);
            throw new BusinessException("刷新令牌无效");
        }
    }

    public void revoke(String refreshToken) {
        if (hasText(refreshToken)) {
            redisTemplate.delete(tokenKey(refreshToken));
            redisTemplate.delete(reusedTokenKey(refreshToken));
        }
    }

    public String rotate(String refreshToken, Long userId) {
        return rotate(refreshToken, userId, "", "");
    }

    public String rotate(String refreshToken, Long userId, String ipaddr, String userAgent) {
        return rotate(refreshToken, userId, ipaddr, userAgent, "");
    }

    public String rotate(String refreshToken, Long userId, String ipaddr, String userAgent, String deviceId) {
        revoke(refreshToken);
        redisTemplate.opsForValue().set(reusedTokenKey(refreshToken), String.valueOf(userId), REUSED_TOKEN_TTL);
        return issue(userId, ipaddr, userAgent, deviceId);
    }

    private String tokenKey(String refreshToken) {
        return REFRESH_TOKEN_KEY_PREFIX + sha256(refreshToken);
    }

    private String reusedTokenKey(String refreshToken) {
        return REFRESH_TOKEN_REUSED_KEY_PREFIX + sha256(refreshToken);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public record RefreshTokenRecord(
            Long userId,
            String ipaddr,
            String userAgent,
            String deviceId,
            String loginTime,
            String refreshTime
    ) {
    }
}

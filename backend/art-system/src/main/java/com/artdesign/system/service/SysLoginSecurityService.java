package com.artdesign.system.service;

import com.artdesign.common.exception.BusinessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;

@Service
public class SysLoginSecurityService {
    private static final String RETRY_KEY_PREFIX = "login:retry:";
    private static final String LOCK_KEY_PREFIX = "login:lock:";
    private static final String RATE_KEY_PREFIX = "login:rate:";
    private static final int DEFAULT_RETRY_COUNT = 5;
    private static final int DEFAULT_LOCK_MINUTES = 10;
    private static final int DEFAULT_RATE_LIMIT = 30;
    private static final Duration RATE_WINDOW = Duration.ofMinutes(1);

    private final RedisTemplate<String, Object> redisTemplate;
    private final SysConfigService configService;

    public SysLoginSecurityService(RedisTemplate<String, Object> redisTemplate, SysConfigService configService) {
        this.redisTemplate = redisTemplate;
        this.configService = configService;
    }

    public void checkBeforeLogin(String userName, String ipaddr) {
        checkRateLimit(userName, ipaddr);
        Object lockValue = redisTemplate.opsForValue().get(lockKey(userName));
        if (lockValue != null) {
            throw new BusinessException("账号已锁定，请稍后再试");
        }
    }

    public void recordLoginFailure(String userName) {
        String retryKey = retryKey(userName);
        Long retryCount = redisTemplate.opsForValue().increment(retryKey);
        if (retryCount != null && retryCount == 1) {
            redisTemplate.expire(retryKey, Duration.ofMinutes(lockMinutes()));
        }
        if (retryCount != null && retryCount >= retryCount()) {
            redisTemplate.opsForValue().set(lockKey(userName), "1", Duration.ofMinutes(lockMinutes()));
            redisTemplate.delete(retryKey);
            throw new BusinessException("密码错误次数过多，账号已锁定");
        }
    }

    public void recordLoginSuccess(String userName) {
        redisTemplate.delete(retryKey(userName));
        redisTemplate.delete(lockKey(userName));
    }

    public void validatePasswordStrength(String password) {
        if (!hasText(password)) {
            throw new BusinessException("密码不能为空");
        }
        if (password.length() < 8) {
            throw new BusinessException("密码长度不能少于8位");
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new BusinessException("密码必须同时包含字母和数字");
        }
    }

    private void checkRateLimit(String userName, String ipaddr) {
        String key = RATE_KEY_PREFIX + safe(ipaddr) + ":" + safe(userName);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, RATE_WINDOW);
        }
        if (count != null && count > rateLimit()) {
            throw new BusinessException("登录请求过于频繁，请稍后再试");
        }
    }

    private int retryCount() {
        return intConfig("sys.account.passwordRetryCount", DEFAULT_RETRY_COUNT);
    }

    private int lockMinutes() {
        return intConfig("sys.account.passwordLockTime", DEFAULT_LOCK_MINUTES);
    }

    private int rateLimit() {
        return intConfig("sys.account.loginRateLimit", DEFAULT_RATE_LIMIT);
    }

    private int intConfig(String key, int fallback) {
        String value = configService.getConfigKey(key);
        if (!hasText(value)) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private String retryKey(String userName) {
        return RETRY_KEY_PREFIX + safe(userName);
    }

    private String lockKey(String userName) {
        return LOCK_KEY_PREFIX + safe(userName);
    }

    private String safe(String value) {
        return hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "unknown";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

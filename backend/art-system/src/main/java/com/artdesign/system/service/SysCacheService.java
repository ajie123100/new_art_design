package com.artdesign.system.service;

import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.CacheNameInfo;
import com.artdesign.system.domain.dto.CacheInfo;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public SysCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public CacheInfo getInfo() {
        try {
            Long dbSize = redisTemplate.execute((RedisCallback<Long>) connection -> connection.dbSize());
            String keys = getCacheKeys();
            Map<String, Object> properties = getProperties();
            return new CacheInfo(
                    "",
                    dbSize != null ? dbSize.toString() : "0",
                    "",
                    keys,
                    properties
            );
        } catch (Exception e) {
            return new CacheInfo("", "0", "", "", Map.of("error", "Redis connection failed"));
        }
    }

    public void clear(String key) {
        redisTemplate.delete(key);
    }

    public List<CacheNameInfo> cacheNames() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        return keys.stream()
                .collect(Collectors.groupingBy(this::cacheName, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new CacheNameInfo(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CacheNameInfo::cacheName))
                .toList();
    }

    public List<String> keys(String cacheName) {
        return sortedKeys(pattern(cacheName));
    }

    public Object value(String cacheName, String key) {
        String redisKey = normalizeKey(cacheName, key);
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            throw new BusinessException("缓存键不存在");
        }
        return redisTemplate.opsForValue().get(redisKey);
    }

    public void clear(String cacheName, String key) {
        redisTemplate.delete(normalizeKey(cacheName, key));
    }

    public void clearCacheName(String cacheName) {
        Set<String> keys = redisTemplate.keys(pattern(cacheName));
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void clearAll() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.flushDb();
            return null;
        });
    }

    private String getCacheKeys() {
        try {
            var keys = redisTemplate.keys("*");
            if (keys == null || keys.isEmpty()) return "";
            StringBuilder sb = new StringBuilder();
            for (Object key : keys) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(key);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private Map<String, Object> getProperties() {
        Map<String, Object> map = new HashMap<>();
        map.put("available", true);
        return map;
    }

    private List<String> sortedKeys(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        return keys.stream().filter(Objects::nonNull).sorted().toList();
    }

    private String cacheName(String key) {
        int colonIndex = key.indexOf(':');
        return colonIndex > 0 ? key.substring(0, colonIndex) : key;
    }

    private String pattern(String cacheName) {
        if (!hasText(cacheName)) {
            throw new BusinessException("缓存名称不能为空");
        }
        return cacheName.endsWith(":") ? cacheName + "*" : cacheName + ":*";
    }

    private String normalizeKey(String cacheName, String key) {
        if (!hasText(key)) {
            throw new BusinessException("缓存键不能为空");
        }
        if (key.contains(":")) {
            return key;
        }
        if (!hasText(cacheName)) {
            throw new BusinessException("缓存名称不能为空");
        }
        return cacheName.endsWith(":") ? cacheName + key : cacheName + ":" + key;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

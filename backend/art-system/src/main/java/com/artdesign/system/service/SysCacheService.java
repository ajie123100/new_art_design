package com.artdesign.system.service;

import com.artdesign.system.domain.dto.CacheInfo;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

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
}

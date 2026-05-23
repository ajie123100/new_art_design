package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.ConfigListItem;
import com.artdesign.system.domain.dto.ConfigSaveRequest;
import com.artdesign.system.domain.entity.SysConfig;
import com.artdesign.system.mapper.SysConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SysConfigService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CONFIG_KEY_PREFIX = "config:";

    private final SysConfigMapper configMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public SysConfigService(SysConfigMapper configMapper, RedisTemplate<String, Object> redisTemplate) {
        this.configMapper = configMapper;
        this.redisTemplate = redisTemplate;
    }

    public PageResult<ConfigListItem> list(Map<String, String> params) {
        long current = parseLong(params.get("current"), 1L);
        long size = parseLong(params.get("size"), 10L);

        List<SysConfig> configs = configMapper.selectList(new LambdaQueryWrapper<SysConfig>()
                .like(hasText(params.get("configName")), SysConfig::getConfigName, params.get("configName"))
                .like(hasText(params.get("configKey")), SysConfig::getConfigKey, params.get("configKey"))
                .eq(hasText(params.get("configType")), SysConfig::getConfigType, params.get("configType"))
                .orderByAsc(SysConfig::getConfigId));

        List<ConfigListItem> records = configs.stream()
                .map(this::toListItem)
                .toList();
        return page(records, current, size);
    }

    public ConfigListItem get(Long configId) {
        return toListItem(findConfig(configId));
    }

    public String getConfigKey(String configKey) {
        Object value = redisTemplate.opsForValue().get(CONFIG_KEY_PREFIX + configKey);
        if (value != null) {
            return value.toString();
        }
        SysConfig config = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey)
                .last("LIMIT 1"));
        if (config == null) {
            return null;
        }
        redisTemplate.opsForValue().set(CONFIG_KEY_PREFIX + configKey, config.getConfigValue(), 30, TimeUnit.MINUTES);
        return config.getConfigValue();
    }

    public Long create(ConfigSaveRequest request) {
        ensureUniqueKey(request.configKey(), null);
        SysConfig config = new SysConfig();
        fillConfig(config, request);
        config.setConfigType(defaultIfBlank(request.configType(), "N"));
        config.setCreateBy("system");
        config.setCreateTime(LocalDateTime.now());
        configMapper.insert(config);
        redisTemplate.opsForValue().set(CONFIG_KEY_PREFIX + config.getConfigKey(), config.getConfigValue(), 30, TimeUnit.MINUTES);
        return config.getConfigId();
    }

    public void update(ConfigSaveRequest request) {
        if (request.configId() == null) {
            throw new BusinessException("参数ID不能为空");
        }
        SysConfig config = findConfig(request.configId());
        ensureUniqueKey(request.configKey(), request.configId());
        fillConfig(config, request);
        config.setConfigType(defaultIfBlank(request.configType(), "N"));
        config.setUpdateBy("system");
        config.setUpdateTime(LocalDateTime.now());
        configMapper.updateById(config);
        redisTemplate.opsForValue().set(CONFIG_KEY_PREFIX + config.getConfigKey(), config.getConfigValue(), 30, TimeUnit.MINUTES);
    }

    public void delete(List<Long> configIds) {
        if (configIds == null || configIds.isEmpty()) {
            throw new BusinessException("请选择要删除的参数");
        }
        for (Long configId : configIds) {
            SysConfig config = findConfig(configId);
            if ("Y".equals(config.getConfigType())) {
                throw new BusinessException("内置参数不能删除");
            }
            configMapper.deleteById(configId);
            redisTemplate.delete(CONFIG_KEY_PREFIX + config.getConfigKey());
        }
    }

    public void loadConfigCache() {
        List<SysConfig> configs = configMapper.selectList(null);
        for (SysConfig config : configs) {
            redisTemplate.opsForValue().set(CONFIG_KEY_PREFIX + config.getConfigKey(), config.getConfigValue(), 30, TimeUnit.MINUTES);
        }
    }

    private SysConfig findConfig(Long configId) {
        SysConfig config = configMapper.selectById(configId);
        if (config == null) {
            throw new BusinessException("参数配置不存在");
        }
        return config;
    }

    private ConfigListItem toListItem(SysConfig config) {
        return new ConfigListItem(
                config.getConfigId(),
                config.getConfigName(),
                config.getConfigKey(),
                config.getConfigValue(),
                config.getConfigType(),
                formatDateTime(config.getCreateTime())
        );
    }

    private void fillConfig(SysConfig config, ConfigSaveRequest request) {
        config.setConfigName(request.configName());
        config.setConfigKey(request.configKey());
        config.setConfigValue(request.configValue());
        config.setRemark(defaultIfBlank(request.remark(), ""));
    }

    private void ensureUniqueKey(String configKey, Long ignoreId) {
        SysConfig existing = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey)
                .last("LIMIT 1"));
        if (existing != null && !existing.getConfigId().equals(ignoreId)) {
            throw new BusinessException("参数键名已存在");
        }
    }

    private <T> PageResult<T> page(List<T> records, long current, long size) {
        int from = (int) Math.min(Math.max(current - 1, 0) * size, records.size());
        int to = (int) Math.min(from + size, records.size());
        return PageResult.of(records.subList(from, to), current, size, records.size());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    private long parseLong(String value, long fallback) {
        if (!hasText(value)) {
            return fallback;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }
}

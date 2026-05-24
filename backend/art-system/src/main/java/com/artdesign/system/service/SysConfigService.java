package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.core.page.PageUtils;
import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.ConfigExcel;
import com.artdesign.system.domain.dto.ConfigListItem;
import com.artdesign.system.domain.dto.ConfigSaveRequest;
import com.artdesign.system.domain.dto.ImportResult;
import com.artdesign.system.domain.entity.SysConfig;
import com.artdesign.system.mapper.SysConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        long current = PageUtils.pageNum(params);
        long size = PageUtils.pageSize(params);

        Page<SysConfig> page = new Page<>(current, size);
        IPage<SysConfig> result = configMapper.selectPage(page, new LambdaQueryWrapper<SysConfig>()
                .like(hasText(params.get("configName")), SysConfig::getConfigName, params.get("configName"))
                .like(hasText(params.get("configKey")), SysConfig::getConfigKey, params.get("configKey"))
                .eq(hasText(params.get("configType")), SysConfig::getConfigType, params.get("configType"))
                .orderByAsc(SysConfig::getConfigId));

        List<ConfigListItem> records = result.getRecords().stream()
                .map(this::toListItem)
                .toList();
        return new PageResult<>(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    public ConfigListItem get(Long configId) {
        return toListItem(findConfig(configId));
    }

    public List<ConfigExcel> exportConfigs(Map<String, String> params) {
        return configMapper.selectList(new LambdaQueryWrapper<SysConfig>()
                        .like(hasText(params.get("configName")), SysConfig::getConfigName, params.get("configName"))
                        .like(hasText(params.get("configKey")), SysConfig::getConfigKey, params.get("configKey"))
                        .eq(hasText(params.get("configType")), SysConfig::getConfigType, params.get("configType"))
                        .orderByAsc(SysConfig::getConfigId))
                .stream()
                .map(this::toConfigExcel)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public ImportResult importConfigs(List<ConfigExcel> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new BusinessException("导入参数不能为空");
        }
        int count = 0;
        int skipped = 0;
        for (ConfigExcel row : rows) {
            if (row == null || !hasText(row.configKey) || !hasText(row.configName)) {
                skipped++;
                continue;
            }
            SysConfig existing = findImportConfig(row);
            ConfigSaveRequest request = new ConfigSaveRequest(
                    existing == null ? row.configId : existing.getConfigId(),
                    row.configName,
                    row.configKey,
                    defaultIfBlank(row.configValue, ""),
                    normalizeConfigType(row.configType),
                    row.remark
            );
            if (existing == null) {
                create(request);
            } else {
                update(request);
            }
            count++;
        }
        if (count == 0) {
            throw new BusinessException("导入参数没有有效数据行");
        }
        return new ImportResult(count, skipped);
    }

    public String getConfigKey(String configKey) {
        try {
            Object value = redisTemplate.opsForValue().get(CONFIG_KEY_PREFIX + configKey);
            if (value != null) {
                return value.toString();
            }
        } catch (Exception ignored) {
        }
        SysConfig config = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey)
                .last("LIMIT 1"));
        if (config == null) {
            return null;
        }
        cacheConfig(config);
        return config.getConfigValue();
    }

    @Transactional(rollbackFor = Exception.class)
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

    @Transactional(rollbackFor = Exception.class)
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

    @Transactional(rollbackFor = Exception.class)
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
            cacheConfig(config);
        }
    }

    public void refreshConfigCache() {
        var keys = redisTemplate.keys(CONFIG_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        loadConfigCache();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadConfigCacheOnReady() {
        try {
            loadConfigCache();
        } catch (Exception ignored) {
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

    private ConfigExcel toConfigExcel(SysConfig config) {
        ConfigExcel excel = new ConfigExcel();
        excel.configId = config.getConfigId();
        excel.configName = config.getConfigName();
        excel.configKey = config.getConfigKey();
        excel.configValue = config.getConfigValue();
        excel.configType = config.getConfigType();
        excel.remark = config.getRemark();
        excel.createTime = formatDateTime(config.getCreateTime());
        return excel;
    }

    private SysConfig findImportConfig(ConfigExcel row) {
        if (row.configId != null) {
            SysConfig config = configMapper.selectById(row.configId);
            if (config != null) {
                return config;
            }
        }
        return configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, row.configKey)
                .last("LIMIT 1"));
    }

    private void fillConfig(SysConfig config, ConfigSaveRequest request) {
        config.setConfigName(request.configName());
        config.setConfigKey(request.configKey());
        config.setConfigValue(request.configValue());
        config.setRemark(defaultIfBlank(request.remark(), ""));
    }

    private void cacheConfig(SysConfig config) {
        try {
            redisTemplate.opsForValue().set(CONFIG_KEY_PREFIX + config.getConfigKey(), config.getConfigValue(), 30, TimeUnit.MINUTES);
        } catch (Exception ignored) {
        }
    }

    private void ensureUniqueKey(String configKey, Long ignoreId) {
        SysConfig existing = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey)
                .last("LIMIT 1"));
        if (existing != null && !existing.getConfigId().equals(ignoreId)) {
            throw new BusinessException("参数键名已存在");
        }
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

    private String normalizeConfigType(String configType) {
        if (!hasText(configType)) {
            return "N";
        }
        return Objects.equals(configType, "是") || Objects.equals(configType, "Y") ? "Y" : "N";
    }
}

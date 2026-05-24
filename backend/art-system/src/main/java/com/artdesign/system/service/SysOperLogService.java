package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.core.page.PageUtils;
import com.artdesign.system.domain.dto.OperLogListItem;
import com.artdesign.system.domain.entity.SysOperLog;
import com.artdesign.system.mapper.SysOperLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class SysOperLogService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysOperLogMapper operLogMapper;

    public SysOperLogService(SysOperLogMapper operLogMapper) {
        this.operLogMapper = operLogMapper;
    }

    @Async
    public void record(SysOperLog operLog) {
        operLogMapper.insert(operLog);
    }

    public PageResult<OperLogListItem> list(Map<String, String> params) {
        long current = PageUtils.pageNum(params);
        long size = PageUtils.pageSize(params);

        Page<SysOperLog> page = new Page<>(current, size);
        IPage<SysOperLog> result = operLogMapper.selectPage(page, buildQuery(params));

        List<OperLogListItem> records = result.getRecords().stream()
                .map(this::toOperLogListItem)
                .toList();
        return new PageResult<>(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    public List<OperLogListItem> exportList(Map<String, String> params) {
        return operLogMapper.selectList(buildQuery(params)).stream().map(this::toOperLogListItem).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void clean() {
        operLogMapper.delete(new LambdaQueryWrapper<>());
    }

    private OperLogListItem toOperLogListItem(SysOperLog log) {
        return new OperLogListItem(
                log.getOperId(),
                log.getTitle(),
                log.getBusinessType(),
                log.getOperName(),
                log.getOperIp(),
                log.getOperUrl(),
                log.getOperParam(),
                log.getStatus(),
                log.getErrorMsg(),
                log.getCostTime(),
                formatDateTime(log.getOperTime())
        );
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    private LambdaQueryWrapper<SysOperLog> buildQuery(Map<String, String> params) {
        Map<String, String> queryParams = params == null ? Map.of() : params;
        Integer businessType = hasText(queryParams.get("businessType")) ? Integer.valueOf(queryParams.get("businessType")) : null;
        return new LambdaQueryWrapper<SysOperLog>()
                .like(hasText(queryParams.get("title")), SysOperLog::getTitle, queryParams.get("title"))
                .like(hasText(queryParams.get("operName")), SysOperLog::getOperName, queryParams.get("operName"))
                .like(hasText(queryParams.get("operIp")), SysOperLog::getOperIp, queryParams.get("operIp"))
                .eq(businessType != null, SysOperLog::getBusinessType, businessType)
                .ge(hasText(queryParams.get("beginTime")), SysOperLog::getOperTime, queryParams.get("beginTime"))
                .le(hasText(queryParams.get("endTime")), SysOperLog::getOperTime, queryParams.get("endTime"))
                .orderByDesc(SysOperLog::getOperTime);
    }

    private long parseLong(String value, long fallback) {
        if (!hasText(value)) return fallback;
        try { return Long.parseLong(value); } catch (NumberFormatException ignored) { return fallback; }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

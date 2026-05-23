package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.system.domain.dto.OperLogListItem;
import com.artdesign.system.domain.entity.SysOperLog;
import com.artdesign.system.mapper.SysOperLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

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

    public void record(SysOperLog operLog) {
        operLogMapper.insert(operLog);
    }

    public PageResult<OperLogListItem> list(Map<String, String> params) {
        long current = parseLong(params.get("current"), 1L);
        long size = parseLong(params.get("size"), 10L);

        List<SysOperLog> logs = operLogMapper.selectList(new LambdaQueryWrapper<SysOperLog>()
                .like(hasText(params.get("title")), SysOperLog::getTitle, params.get("title"))
                .like(hasText(params.get("operName")), SysOperLog::getOperName, params.get("operName"))
                .like(hasText(params.get("operIp")), SysOperLog::getOperIp, params.get("operIp"))
                .eq(hasText(params.get("businessType")), SysOperLog::getBusinessType, Integer.valueOf(params.get("businessType")))
                .ge(hasText(params.get("beginTime")), SysOperLog::getOperTime, params.get("beginTime"))
                .le(hasText(params.get("endTime")), SysOperLog::getOperTime, params.get("endTime"))
                .orderByDesc(SysOperLog::getOperTime));

        List<OperLogListItem> records = logs.stream()
                .map(this::toOperLogListItem)
                .toList();
        return page(records, current, size);
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
}

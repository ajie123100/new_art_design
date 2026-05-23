package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.system.domain.dto.JobLogListItem;
import com.artdesign.system.domain.entity.SysJobLog;
import com.artdesign.system.mapper.SysJobLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class SysJobLogService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysJobLogMapper jobLogMapper;

    public SysJobLogService(SysJobLogMapper jobLogMapper) {
        this.jobLogMapper = jobLogMapper;
    }

    public PageResult<JobLogListItem> list(Map<String, String> params) {
        long current = parseLong(params.get("current"), 1L);
        long size = parseLong(params.get("size"), 10L);
        List<SysJobLog> logs = jobLogMapper.selectList(new LambdaQueryWrapper<SysJobLog>()
                .like(hasText(params.get("jobName")), SysJobLog::getJobName, params.get("jobName"))
                .eq(hasText(params.get("jobGroup")), SysJobLog::getJobGroup, params.get("jobGroup"))
                .orderByDesc(SysJobLog::getJobLogId));
        List<JobLogListItem> records = logs.stream().map(this::toListItem).toList();
        return page(records, current, size);
    }

    private JobLogListItem toListItem(SysJobLog log) {
        return new JobLogListItem(
                log.getJobLogId(), log.getJobName(), log.getJobGroup(),
                log.getInvokeTarget(), log.getJobMessage(),
                log.getStatus(), formatDateTime(log.getCreateTime())
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
        if (value == null || value.trim().isEmpty()) return fallback;
        try { return Long.parseLong(value); } catch (NumberFormatException ignored) { return fallback; }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

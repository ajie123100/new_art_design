package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.core.page.PageUtils;
import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.JobLogListItem;
import com.artdesign.system.domain.entity.SysJobLog;
import com.artdesign.system.mapper.SysJobLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class SysJobLogService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysJobLogMapper jobLogMapper;
    private final JdbcTemplate jdbcTemplate;

    public SysJobLogService(SysJobLogMapper jobLogMapper, JdbcTemplate jdbcTemplate) {
        this.jobLogMapper = jobLogMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureSchema() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'sys_job_log'
                  AND COLUMN_NAME = 'elapsed_ms'
                """, Integer.class);
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE sys_job_log ADD COLUMN elapsed_ms BIGINT DEFAULT 0 COMMENT '执行耗时（毫秒）' AFTER exception_info");
        }
    }

    public PageResult<JobLogListItem> list(Map<String, String> params) {
        long current = PageUtils.pageNum(params);
        long size = PageUtils.pageSize(params);
        Page<SysJobLog> page = new Page<>(current, size);
        IPage<SysJobLog> result = jobLogMapper.selectPage(page, buildQuery(params));
        List<JobLogListItem> records = result.getRecords().stream().map(this::toListItem).toList();
        return new PageResult<>(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    public List<JobLogListItem> exportList(Map<String, String> params) {
        return jobLogMapper.selectList(buildQuery(params)).stream().map(this::toListItem).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的任务日志");
        }
        jobLogMapper.deleteBatchIds(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    public void clean() {
        jobLogMapper.delete(new LambdaQueryWrapper<>());
    }

    private JobLogListItem toListItem(SysJobLog log) {
        return new JobLogListItem(
                log.getJobLogId(), log.getJobName(), log.getJobGroup(),
                log.getInvokeTarget(), log.getJobMessage(),
                log.getStatus(), log.getElapsedMs(), formatDateTime(log.getCreateTime())
        );
    }

    private LambdaQueryWrapper<SysJobLog> buildQuery(Map<String, String> params) {
        Map<String, String> queryParams = params == null ? Map.of() : params;
        return new LambdaQueryWrapper<SysJobLog>()
                .like(hasText(queryParams.get("jobName")), SysJobLog::getJobName, queryParams.get("jobName"))
                .eq(hasText(queryParams.get("jobGroup")), SysJobLog::getJobGroup, queryParams.get("jobGroup"))
                .eq(hasText(queryParams.get("status")), SysJobLog::getStatus, queryParams.get("status"))
                .orderByDesc(SysJobLog::getJobLogId);
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

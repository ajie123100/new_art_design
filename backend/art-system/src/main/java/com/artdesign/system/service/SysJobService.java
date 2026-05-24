package com.artdesign.system.service;

import com.artdesign.common.core.page.PageResult;
import com.artdesign.common.core.page.PageUtils;
import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.JobListItem;
import com.artdesign.system.domain.dto.JobSaveRequest;
import com.artdesign.system.domain.entity.SysJob;
import com.artdesign.system.mapper.SysJobLogMapper;
import com.artdesign.system.mapper.SysJobMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class SysJobService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysJobMapper jobMapper;
    private final SysJobLogMapper jobLogMapper;
    private final Scheduler scheduler;

    public SysJobService(SysJobMapper jobMapper, SysJobLogMapper jobLogMapper, Scheduler scheduler) {
        this.jobMapper = jobMapper;
        this.jobLogMapper = jobLogMapper;
        this.scheduler = scheduler;
    }

    public PageResult<JobListItem> list(Map<String, String> params) {
        long current = PageUtils.pageNum(params);
        long size = PageUtils.pageSize(params);
        Page<SysJob> page = new Page<>(current, size);
        IPage<SysJob> result = jobMapper.selectPage(page, new LambdaQueryWrapper<SysJob>()
                .like(hasText(params.get("jobName")), SysJob::getJobName, params.get("jobName"))
                .eq(hasText(params.get("jobGroup")), SysJob::getJobGroup, params.get("jobGroup"))
                .eq(hasText(params.get("status")), SysJob::getStatus, params.get("status"))
                .orderByDesc(SysJob::getJobId));
        List<JobListItem> records = result.getRecords().stream().map(this::toListItem).toList();
        return new PageResult<>(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    public JobListItem get(Long jobId) {
        return toListItem(findJob(jobId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(JobSaveRequest request) {
        validateCron(request.cronExpression());
        SysJob job = new SysJob();
        fillJob(job, request);
        job.setJobGroup(defaultIfBlank(request.jobGroup(), "DEFAULT"));
        job.setMisfirePolicy(defaultIfBlank(request.misfirePolicy(), "3"));
        job.setConcurrent(defaultIfBlank(request.concurrent(), "1"));
        job.setStatus(defaultIfBlank(request.status(), "0"));
        job.setCreateBy("system");
        job.setCreateTime(LocalDateTime.now());
        jobMapper.insert(job);
        scheduleJob(job);
        return job.getJobId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(JobSaveRequest request) {
        if (request.jobId() == null) {
            throw new BusinessException("任务ID不能为空");
        }
        validateCron(request.cronExpression());
        SysJob job = findJob(request.jobId());
        fillJob(job, request);
        job.setJobGroup(defaultIfBlank(request.jobGroup(), "DEFAULT"));
        job.setMisfirePolicy(defaultIfBlank(request.misfirePolicy(), "3"));
        job.setConcurrent(defaultIfBlank(request.concurrent(), "1"));
        job.setUpdateBy("system");
        job.setUpdateTime(LocalDateTime.now());
        jobMapper.updateById(job);
        rescheduleJob(job);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> jobIds) {
        if (jobIds == null || jobIds.isEmpty()) {
            throw new BusinessException("请选择要删除的任务");
        }
        for (Long jobId : jobIds) {
            SysJob job = findJob(jobId);
            deleteScheduleJob(job);
        }
        jobMapper.deleteBatchIds(jobIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long jobId, String status) {
        SysJob job = findJob(jobId);
        job.setStatus(status);
        job.setUpdateBy("system");
        job.setUpdateTime(LocalDateTime.now());
        jobMapper.updateById(job);
        if ("0".equals(status)) {
            scheduleJob(job);
        } else {
            deleteScheduleJob(job);
        }
    }

    public void run(List<Long> jobIds) {
        for (Long jobId : jobIds) {
            SysJob job = findJob(jobId);
            try {
                JobKey jobKey = new JobKey(getJobKey(job), getGroup(job));
                if (scheduler.checkExists(jobKey)) {
                    scheduler.triggerJob(jobKey);
                    continue;
                }
                JobDetail jobDetail = buildJobDetail(job, "RUN_" + job.getJobId() + "_" + System.nanoTime());
                Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity("RUN_TRIGGER_" + job.getJobId() + "_" + System.nanoTime(), getGroup(job))
                        .startNow()
                        .build();
                scheduler.scheduleJob(jobDetail, trigger);
            } catch (SchedulerException e) {
                throw new BusinessException("任务执行失败: " + e.getMessage());
            }
        }
    }

    private void scheduleJob(SysJob job) {
        try {
            if (scheduler.checkExists(new JobKey(getJobKey(job), getGroup(job)))) {
                return;
            }
            if (!"0".equals(job.getStatus())) {
                return;
            }
            JobDetail jobDetail = buildJobDetail(job, getJobKey(job));
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression())
                    .withMisfireHandlingInstructionDoNothing();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(getTriggerKey(job), getGroup(job))
                    .withSchedule(scheduleBuilder)
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new BusinessException("任务调度异常: " + e.getMessage());
        }
    }

    private JobDetail buildJobDetail(SysJob job, String jobKey) {
        JobDetail jobDetail = JobBuilder.newJob(TasksJob.class)
                .withIdentity(jobKey, getGroup(job))
                .build();
        JobDataMap dataMap = jobDetail.getJobDataMap();
        dataMap.put("jobId", job.getJobId());
        dataMap.put("invokeTarget", job.getInvokeTarget());
        return jobDetail;
    }

    private void rescheduleJob(SysJob job) {
        try {
            JobKey jobKey = new JobKey(getJobKey(job), getGroup(job));
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            // ignore
        }
        scheduleJob(job);
    }

    private void deleteScheduleJob(SysJob job) {
        try {
            scheduler.deleteJob(new JobKey(getJobKey(job), getGroup(job)));
        } catch (SchedulerException ignored) {
        }
    }

    private String getJobKey(SysJob job) {
        return "JOB_" + job.getJobId();
    }

    private String getTriggerKey(SysJob job) {
        return "TRIGGER_" + job.getJobId();
    }

    private String getGroup(SysJob job) {
        return job.getJobGroup() != null ? job.getJobGroup() : "DEFAULT";
    }

    private void validateCron(String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            throw new BusinessException("Cron表达式格式错误");
        }
    }

    private SysJob findJob(Long jobId) {
        SysJob job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException("任务不存在");
        }
        return job;
    }

    private JobListItem toListItem(SysJob job) {
        return new JobListItem(
                job.getJobId(), job.getJobName(), job.getJobGroup(),
                job.getInvokeTarget(), job.getCronExpression(),
                job.getStatus(), formatDateTime(job.getCreateTime())
        );
    }

    private void fillJob(SysJob job, JobSaveRequest request) {
        job.setJobName(request.jobName());
        job.setInvokeTarget(request.invokeTarget());
        job.setCronExpression(request.cronExpression());
        job.setRemark(defaultIfBlank(request.remark(), ""));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    private long parseLong(String value, long fallback) {
        if (!hasText(value)) return fallback;
        try { return Long.parseLong(value); } catch (NumberFormatException ignored) { return fallback; }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }
}

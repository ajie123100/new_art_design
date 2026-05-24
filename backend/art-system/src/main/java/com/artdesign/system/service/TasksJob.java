package com.artdesign.system.service;

import com.artdesign.system.domain.entity.SysJobLog;
import com.artdesign.system.mapper.SysJobLogMapper;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

public class TasksJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long start = System.currentTimeMillis();
        String invokeTarget = context.getMergedJobDataMap().getString("invokeTarget");
        Long jobId = context.getMergedJobDataMap().getLongValue("jobId");
        SysJobLog log = new SysJobLog();
        log.setJobName("JOB_" + jobId);
        log.setJobGroup(context.getJobDetail().getKey().getGroup());
        log.setInvokeTarget(invokeTarget);
        log.setCreateTime(LocalDateTime.now());
        try {
            new InvokeTarget(invokeTarget).invoke();
            log.setStatus("0");
            log.setJobMessage("success");
        } catch (Exception e) {
            log.setStatus("1");
            log.setExceptionInfo(e.getMessage());
            log.setJobMessage(e.getMessage());
        } finally {
            log.setElapsedMs(System.currentTimeMillis() - start);
            SpringContextUtils.getBean(SysJobLogMapper.class).insert(log);
        }
    }

    public static class InvokeTarget {
        private final String invokeTarget;

        public InvokeTarget(String invokeTarget) {
            this.invokeTarget = invokeTarget;
        }

        public void invoke() throws Exception {
            int dotIndex = invokeTarget.lastIndexOf('.');
            if (dotIndex <= 0 || dotIndex == invokeTarget.length() - 1) {
                throw new IllegalArgumentException("Invalid invoke target: " + invokeTarget);
            }
            String className = invokeTarget.substring(0, dotIndex);
            String methodName = invokeTarget.substring(dotIndex + 1);
            Class<?> clazz = Class.forName(className);
            Object bean = SpringContextUtils.getBean(clazz);
            Method method = clazz.getMethod(methodName);
            ReflectionUtils.invokeMethod(method, bean);
        }
    }
}

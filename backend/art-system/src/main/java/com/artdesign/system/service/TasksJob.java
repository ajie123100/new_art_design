package com.artdesign.system.service;

import com.artdesign.system.domain.entity.SysJobLog;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

public class TasksJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String invokeTarget = context.getMergedJobDataMap().getString("invokeTarget");
        Long jobId = context.getMergedJobDataMap().getLongValue("jobId");
        SysJobLog log = new SysJobLog();
        log.setJobName("JOB_" + jobId);
        log.setJobGroup(context.getJobDetail().getKey().getGroup());
        log.setInvokeTarget(invokeTarget);
        log.setCreateTime(LocalDateTime.now());
        try {
            new InvokeTarget(invokeTarget).invoke();
            log.setStatus("1");
            log.setJobMessage("success");
        } catch (Exception e) {
            log.setStatus("0");
            log.setExceptionInfo(e.getMessage());
            log.setJobMessage(e.getMessage());
        }
    }

    public static class InvokeTarget {
        private final String invokeTarget;

        public InvokeTarget(String invokeTarget) {
            this.invokeTarget = invokeTarget;
        }

        public void invoke() throws Exception {
            String[] parts = invokeTarget.split("\\.");
            if (parts.length < 3) {
                throw new IllegalArgumentException("Invalid invoke target: " + invokeTarget);
            }
            String className = parts[0] + "." + parts[1] + "." + parts[2];
            String methodName = parts.length > 3 ? parts[3] : "execute";
            Class<?> clazz = Class.forName(className);
            Object bean = SpringContextUtils.getBean(clazz);
            Method method = clazz.getMethod(methodName);
            ReflectionUtils.invokeMethod(method, bean);
        }
    }
}

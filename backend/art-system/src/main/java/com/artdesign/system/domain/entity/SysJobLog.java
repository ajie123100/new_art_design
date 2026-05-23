package com.artdesign.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("sys_job_log")
public class SysJobLog {
    @TableId
    private Long jobLogId;
    private String jobName;
    private String jobGroup;
    private String invokeTarget;
    private String jobMessage;
    private String status;
    private String exceptionInfo;
    private LocalDateTime createTime;

    public Long getJobLogId() { return jobLogId; }
    public void setJobLogId(Long jobLogId) { this.jobLogId = jobLogId; }
    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    public String getJobGroup() { return jobGroup; }
    public void setJobGroup(String jobGroup) { this.jobGroup = jobGroup; }
    public String getInvokeTarget() { return invokeTarget; }
    public void setInvokeTarget(String invokeTarget) { this.invokeTarget = invokeTarget; }
    public String getJobMessage() { return jobMessage; }
    public void setJobMessage(String jobMessage) { this.jobMessage = jobMessage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getExceptionInfo() { return exceptionInfo; }
    public void setExceptionInfo(String exceptionInfo) { this.exceptionInfo = exceptionInfo; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

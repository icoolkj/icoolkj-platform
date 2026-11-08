package com.icoolkj.platform.system.job.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 平台定时任务执行日志 — maps to {@code sys_job_log}. */
@TableName("sys_job_log")
@Getter
@Setter
public class SysJobLogEntity {

    @TableId("id")
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("job_id")
    private String jobId;

    @TableField("job_code")
    private String jobCode;

    @TableField("trigger_type")
    private String triggerType;

    @TableField("trigger_user_id")
    private String triggerUserId;

    private String status;

    @TableField("job_message")
    private String jobMessage;

    @TableField("exception_info")
    private String exceptionInfo;

    @TableField("request_param")
    private String requestParam;

    @TableField("result_summary")
    private String resultSummary;

    @TableField("started_at")
    private LocalDateTime startedAt;

    @TableField("finished_at")
    private LocalDateTime finishedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

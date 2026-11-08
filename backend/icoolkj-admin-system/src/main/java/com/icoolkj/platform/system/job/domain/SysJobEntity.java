package com.icoolkj.platform.system.job.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 平台定时任务定义 — maps to {@code sys_job}. */
@TableName("sys_job")
@Getter
@Setter
public class SysJobEntity extends TenantBaseEntity {

    @TableField("job_code")
    private String jobCode;

    @TableField("job_name")
    private String jobName;

    @TableField("job_group")
    private String jobGroup;

    @TableField("cron_expression")
    private String cronExpression;

    @TableField("misfire_policy")
    private String misfirePolicy;

    @TableField("allow_concurrent")
    private String allowConcurrent;

    private String status;

    private String remark;
}

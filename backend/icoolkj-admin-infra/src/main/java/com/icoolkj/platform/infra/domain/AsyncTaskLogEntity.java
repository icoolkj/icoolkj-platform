package com.icoolkj.platform.infra.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 异步任务日志 — maps to `infra_async_task_log`. */
@TableName("infra_async_task_log")
@Getter
@Setter
public class AsyncTaskLogEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("task_id")
    private String taskId;

    @TableField("log_level")
    private String logLevel;

    @TableField("log_content")
    private String logContent;

    @TableField("created_at")
    private LocalDateTime createdAt;

}

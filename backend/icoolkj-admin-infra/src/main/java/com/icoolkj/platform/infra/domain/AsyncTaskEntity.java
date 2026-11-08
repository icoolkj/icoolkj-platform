package com.icoolkj.platform.infra.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 异步任务 — maps to `infra_async_task`. */
@TableName("infra_async_task")
@Getter
@Setter
public class AsyncTaskEntity extends TenantBaseEntity {

    @TableField("task_type")
    private String taskType;

    @TableField("task_name")
    private String taskName;

    @TableField("biz_type")
    private String bizType;

    @TableField("biz_id")
    private String bizId;

    @TableField("task_status")
    private String taskStatus;

    @TableField("request_param")
    private String requestParam;

    @TableField("result_data")
    private String resultData;

    @TableField("error_message")
    private String errorMessage;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("max_retry_count")
    private Integer maxRetryCount;

    @TableField("requeue_count")
    private Integer requeueCount;

    @TableField("started_at")
    private LocalDateTime startedAt;

    @TableField("finished_at")
    private LocalDateTime finishedAt;

    @TableField("user_id")
    private String userId;

    /** Submitter locale tag (e.g. zh-CN) for worker-side i18n (Excel export headers). */
    @TableField("locale")
    private String locale;

}

package com.icoolkj.platform.infra.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 异步任务表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AsyncTaskPageQuery extends PageQuery {
    private String tenantId;
    private String taskType;
    private String taskName;
    private String bizType;
    private String bizId;
    private String taskStatus;
    private String requestParam;
    private String resultData;
    private String errorMessage;
    private Integer retryCount;
    private Integer maxRetryCount;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    /**
     * Submitter filter. Service always binds current user so peers cannot list/delete others' tasks
     * (delete releases excel-import/export staging).
     */
    private String userId;
}

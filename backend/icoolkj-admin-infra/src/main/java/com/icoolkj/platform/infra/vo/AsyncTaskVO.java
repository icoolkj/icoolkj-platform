package com.icoolkj.platform.infra.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 异步任务表列表/详情 */
@Data
public class AsyncTaskVO {
    private String id;
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
    private Integer requeueCount;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String userId;
    private LocalDateTime createdAt;
}

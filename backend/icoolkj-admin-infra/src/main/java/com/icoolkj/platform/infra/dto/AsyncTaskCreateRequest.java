package com.icoolkj.platform.infra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

/** 新增异步任务表 */
@Data
public class AsyncTaskCreateRequest {
    @NotBlank(message = "{gen.asyncTask.tenantId.required}")
    @Size(max = 20, message = "{gen.asyncTask.tenantId.maxLength}")
    private String tenantId;
    @NotBlank(message = "{gen.asyncTask.taskType.required}")
    @Size(max = 50, message = "{gen.asyncTask.taskType.maxLength}")
    private String taskType;
    @NotBlank(message = "{gen.asyncTask.taskName.required}")
    @Size(max = 100, message = "{gen.asyncTask.taskName.maxLength}")
    private String taskName;
    @Size(max = 50, message = "{gen.asyncTask.bizType.maxLength}")
    private String bizType;
    @Size(max = 20, message = "{gen.asyncTask.bizId.maxLength}")
    private String bizId;
    @NotBlank(message = "{gen.asyncTask.taskStatus.required}")
    @Size(max = 1, message = "{gen.asyncTask.taskStatus.maxLength}")
    private String taskStatus;
    @Size(max = 65535, message = "{gen.asyncTask.requestParam.maxLength}")
    private String requestParam;
    @Size(max = 65535, message = "{gen.asyncTask.resultData.maxLength}")
    private String resultData;
    @Size(max = 65535, message = "{gen.asyncTask.errorMessage.maxLength}")
    private String errorMessage;
    @NotNull(message = "{gen.asyncTask.retryCount.required}")
    private Integer retryCount;
    @NotNull(message = "{gen.asyncTask.maxRetryCount.required}")
    private Integer maxRetryCount;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}

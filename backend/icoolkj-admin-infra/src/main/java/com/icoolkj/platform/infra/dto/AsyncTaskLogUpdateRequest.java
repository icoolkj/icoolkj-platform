package com.icoolkj.platform.infra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

/** 修改异步任务日志表 */
@Data
public class AsyncTaskLogUpdateRequest {

    @NotBlank(message = "{validation.id.required}")
    private String id;
    @NotBlank(message = "{gen.asyncTaskLog.tenantId.required}")
    @Size(max = 20, message = "{gen.asyncTaskLog.tenantId.maxLength}")
    private String tenantId;
    @NotBlank(message = "{gen.asyncTaskLog.taskId.required}")
    @Size(max = 20, message = "{gen.asyncTaskLog.taskId.maxLength}")
    private String taskId;
    @NotBlank(message = "{gen.asyncTaskLog.logLevel.required}")
    @Size(max = 20, message = "{gen.asyncTaskLog.logLevel.maxLength}")
    private String logLevel;
    @NotBlank(message = "{gen.asyncTaskLog.logContent.required}")
    @Size(max = 65535, message = "{gen.asyncTaskLog.logContent.maxLength}")
    private String logContent;
}

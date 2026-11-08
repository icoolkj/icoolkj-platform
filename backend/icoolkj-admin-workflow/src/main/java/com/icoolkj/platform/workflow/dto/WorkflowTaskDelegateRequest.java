package com.icoolkj.platform.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class WorkflowTaskDelegateRequest {

    @NotBlank(message = "{error.workflow.delegate_target_required}")
    @Size(max = 20)
    private String targetUserId;

    @Size(max = 500)
    private String comment;

    /** 委派截止时间；空=直至任务完成 */
    private LocalDateTime delegateUntil;
}

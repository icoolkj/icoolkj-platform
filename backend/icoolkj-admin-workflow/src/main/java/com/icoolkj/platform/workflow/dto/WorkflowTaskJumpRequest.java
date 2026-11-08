package com.icoolkj.platform.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkflowTaskJumpRequest {

    @NotBlank(message = "{error.workflow.jump_target_required}")
    @Size(max = 64)
    private String targetActivityId;

    @Size(max = 500)
    private String comment;
}

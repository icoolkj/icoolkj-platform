package com.icoolkj.platform.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkflowTaskTransferRequest {

    @NotBlank(message = "{error.workflow.transfer_target_required}")
    @Size(max = 20)
    private String targetUserId;

    @Size(max = 500)
    private String comment;
}

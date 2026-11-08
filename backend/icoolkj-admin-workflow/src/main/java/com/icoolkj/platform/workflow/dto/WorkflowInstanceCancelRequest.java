package com.icoolkj.platform.workflow.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** 流程实例撤回请求 */
@Data
public class WorkflowInstanceCancelRequest {

    @Size(max = 500, message = "{error.workflow.cancel_reason_too_long}")
    private String reason;
}

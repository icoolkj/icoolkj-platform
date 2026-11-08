package com.icoolkj.platform.workflow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Data;

/** 任务审批请求 */
@Data
public class WorkflowTaskCompleteRequest {

    @NotNull(message = "{gen.workflowTaskComplete.approved.required}")
    private Boolean approved;

    @Size(max = 500, message = "{gen.workflowTaskComplete.comment.maxLength}")
    private String comment;

    /**
     * 审批人改写的表单字段；服务端仅合并节点 fieldPerms=W 的键。
     */
    private Map<String, Object> formData;
}

package com.icoolkj.platform.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 流程模型更新请求 */
@Data
public class WorkflowModelUpdateRequest {

    @NotBlank(message = "{validation.id.required}")
    private String id;

    @NotBlank(message = "{gen.workflowModel.modelName.required}")
    @Size(max = 100, message = "{gen.workflowModel.modelName.maxLength}")
    private String modelName;

    @Size(max = 20, message = "{gen.workflowModel.categoryId.maxLength}")
    private String categoryId;

    @Size(max = 20, message = "{gen.workflowModel.formType.maxLength}")
    private String formType;

    @Size(max = 200, message = "{gen.workflowModel.formRoute.maxLength}")
    private String formRoute;

    @Size(max = 1, message = "{gen.workflowModel.status.maxLength}")
    private String status;

    @Size(max = 500, message = "{gen.workflowModel.remark.maxLength}")
    private String remark;
}

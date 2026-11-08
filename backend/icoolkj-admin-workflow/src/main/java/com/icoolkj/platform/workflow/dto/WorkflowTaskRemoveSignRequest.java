package com.icoolkj.platform.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class WorkflowTaskRemoveSignRequest {

    /** 待减签的办理人（须为本节点其他活跃会签/或签办理人） */
    @NotEmpty(message = "{error.workflow.remove_sign_users_required}")
    private List<@NotBlank @Size(max = 20) String> userIds;

    @Size(max = 500)
    private String comment;
}

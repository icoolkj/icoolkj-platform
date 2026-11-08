package com.icoolkj.platform.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class WorkflowTaskAddSignRequest {

    /** before | after */
    @NotBlank
    @Size(max = 16)
    private String mode;

    @NotEmpty(message = "{error.workflow.add_sign_users_required}")
    private List<@NotBlank @Size(max = 20) String> userIds;

    @Size(max = 500)
    private String comment;
}

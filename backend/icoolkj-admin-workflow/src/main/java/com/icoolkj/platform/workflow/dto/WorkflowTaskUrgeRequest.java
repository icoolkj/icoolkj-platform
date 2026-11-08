package com.icoolkj.platform.workflow.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkflowTaskUrgeRequest {

    @Size(max = 500)
    private String comment;
}

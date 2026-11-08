package com.icoolkj.platform.system.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiModelSaveRequest {
    private String id;

    @NotBlank
    private String providerId;

    @NotBlank
    @Size(max = 128)
    private String modelCode;

    @NotBlank
    @Size(max = 128)
    private String modelName;

    @NotBlank
    @Size(max = 128)
    private String capabilities;

    private String defaultParamsJson;

    @Size(max = 1)
    private String status;

    private Integer sortNo;

    @Size(max = 500)
    private String remark;
}

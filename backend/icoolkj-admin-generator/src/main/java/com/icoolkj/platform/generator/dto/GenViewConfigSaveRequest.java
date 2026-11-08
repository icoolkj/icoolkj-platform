package com.icoolkj.platform.generator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenViewConfigSaveRequest {

    private String id;

    @NotBlank
    private String tableId;

    @NotBlank
    private String viewJson;

    private String remark;
}

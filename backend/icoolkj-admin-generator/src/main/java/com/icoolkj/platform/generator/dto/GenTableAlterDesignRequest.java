package com.icoolkj.platform.generator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GenTableAlterDesignRequest {

    @NotBlank
    @Size(max = 128)
    private String tableName;

    @NotBlank
    @Size(max = 4000)
    private String userPrompt;
}

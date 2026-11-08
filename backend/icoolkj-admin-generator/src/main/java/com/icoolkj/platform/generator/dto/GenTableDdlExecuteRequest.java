package com.icoolkj.platform.generator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GenTableDdlExecuteRequest {

    @NotBlank
    @Size(max = 32000)
    private String ddl;
}

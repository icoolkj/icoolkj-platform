package com.icoolkj.platform.generator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GenTablePrepareRequest {

    @NotBlank
    @Pattern(regexp = "^[a-z][a-z0-9_]{1,62}$")
    private String tableName;
}

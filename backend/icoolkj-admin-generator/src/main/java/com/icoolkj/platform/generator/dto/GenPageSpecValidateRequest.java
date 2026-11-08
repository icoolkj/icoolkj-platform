package com.icoolkj.platform.generator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 校验 page-spec.yaml 文本 */
@Data
public class GenPageSpecValidateRequest {

    @NotBlank
    @Size(max = 100000)
    private String yamlContent;

    /** 可选：用于补全 meta / entity */
    private String tableId;
}

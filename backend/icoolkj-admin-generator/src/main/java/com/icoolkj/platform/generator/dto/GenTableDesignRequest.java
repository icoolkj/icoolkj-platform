package com.icoolkj.platform.generator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GenTableDesignRequest {

    @NotBlank
    @Size(max = 4000)
    private String userPrompt;

    /** system | tenant | infra | biz，默认 system */
    private String apiModule;
}

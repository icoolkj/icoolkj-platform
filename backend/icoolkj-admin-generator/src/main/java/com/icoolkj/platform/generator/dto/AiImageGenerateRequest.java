package com.icoolkj.platform.generator.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiImageGenerateRequest {

    @NotBlank(message = "{error.ai.image_prompt_required}")
    @Size(max = 2000, message = "{error.ai.image_prompt_too_long}")
    private String prompt;

    @Size(max = 32)
    private String size;

    @Min(1)
    @Max(6)
    private Integer n;

    @Size(max = 500)
    private String negativePrompt;

    @Size(max = 32)
    private String responseFormat;

    private Boolean promptExtend;

    private Boolean watermark;
}

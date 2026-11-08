package com.icoolkj.platform.system.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiCapabilityBindRequest {

    @NotBlank
    private String capability;

    private String modelId;
}

package com.icoolkj.platform.system.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiProviderTenantKeyRequest {

    @NotBlank
    private String apiKey;
}

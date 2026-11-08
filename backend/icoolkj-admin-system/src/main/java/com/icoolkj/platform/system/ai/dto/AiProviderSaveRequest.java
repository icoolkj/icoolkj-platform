package com.icoolkj.platform.system.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiProviderSaveRequest {
    private String id;

    @NotBlank
    @Size(max = 64)
    private String providerCode;

    @NotBlank
    @Size(max = 128)
    private String providerName;

    @Size(max = 32)
    private String protocol;

    @Size(max = 512)
    private String baseUrl;

    @Size(max = 512)
    private String apiKey;

    private Integer timeoutSeconds;

    @Size(max = 1)
    private String status;

    @Size(max = 500)
    private String remark;
}

package com.icoolkj.platform.system.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantPolicyConfigUpdateRequest {

    @NotBlank(message = "{gen.tenantPolicy.configValue.required}")
    @Size(max = 65535, message = "{gen.tenantPolicy.configValue.maxLength}")
    private String configValue;
}

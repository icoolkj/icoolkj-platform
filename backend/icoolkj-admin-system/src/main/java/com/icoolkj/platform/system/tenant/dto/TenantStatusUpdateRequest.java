package com.icoolkj.platform.system.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 租户启用/停用 */
@Data
public class TenantStatusUpdateRequest {

    @NotBlank(message = "{validation.id.required}")
    private String id;

    @NotBlank(message = "{gen.tenant.status.required}")
    @Pattern(regexp = "[01]", message = "{gen.tenant.status.required}")
    @Size(max = 1, message = "{gen.tenant.status.maxLength}")
    private String status;
}

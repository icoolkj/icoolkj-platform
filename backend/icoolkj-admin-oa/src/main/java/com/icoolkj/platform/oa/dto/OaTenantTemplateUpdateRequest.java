package com.icoolkj.platform.oa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 更新 OA 租户模板映射 */
@Getter
@Setter
public class OaTenantTemplateUpdateRequest {

    @NotBlank(message = "{validation.id.required}")
    private String id;

    @NotBlank(message = "{gen.oaTenantTemplate.templateName.required}")
    @Size(max = 100, message = "{gen.oaTenantTemplate.templateName.maxLength}")
    private String templateName;

    @Size(max = 1, message = "{gen.oaTenantTemplate.status.maxLength}")
    private String status;

    @Size(max = 500, message = "{gen.oaTenantTemplate.remark.maxLength}")
    private String remark;
}

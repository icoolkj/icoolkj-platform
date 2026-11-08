package com.icoolkj.platform.oa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 启用 OA 租户模板映射 */
@Getter
@Setter
public class OaTenantEnableRequest {

    @NotBlank(message = "{gen.oaTenantTemplate.platformTemplateId.required}")
    private String platformTemplateId;

    @Size(max = 100, message = "{gen.oaTenantTemplate.templateName.maxLength}")
    private String templateName;

    @Size(max = 500, message = "{gen.oaTenantTemplate.remark.maxLength}")
    private String remark;
}

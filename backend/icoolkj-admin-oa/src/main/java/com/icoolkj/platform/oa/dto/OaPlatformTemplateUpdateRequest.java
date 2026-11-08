package com.icoolkj.platform.oa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 更新 OA 平台模板 */
@Getter
@Setter
public class OaPlatformTemplateUpdateRequest {

    @NotBlank(message = "{validation.id.required}")
    private String id;

    @NotBlank(message = "{gen.oaPlatformTemplate.templateName.required}")
    @Size(max = 100, message = "{gen.oaPlatformTemplate.templateName.maxLength}")
    private String templateName;

    @Size(max = 20, message = "{gen.oaPlatformTemplate.wfModelId.maxLength}")
    private String wfModelId;

    @Size(max = 1, message = "{gen.oaPlatformTemplate.status.maxLength}")
    private String status;

    @Size(max = 500, message = "{gen.oaPlatformTemplate.remark.maxLength}")
    private String remark;
}

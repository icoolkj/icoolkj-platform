package com.icoolkj.platform.oa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 新增 OA 平台模板 */
@Getter
@Setter
public class OaPlatformTemplateCreateRequest {

    @NotBlank(message = "{gen.oaPlatformTemplate.categoryCode.required}")
    @Size(max = 64, message = "{gen.oaPlatformTemplate.categoryCode.maxLength}")
    private String categoryCode;

    @NotBlank(message = "{gen.oaPlatformTemplate.templateCode.required}")
    @Size(max = 64, message = "{gen.oaPlatformTemplate.templateCode.maxLength}")
    private String templateCode;

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

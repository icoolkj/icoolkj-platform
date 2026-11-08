package com.icoolkj.platform.system.i18n.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 修改平台国际化文案表 */
@Data
public class I18nMessageUpdateRequest {

    @NotBlank(message = "{validation.id.required}")
    private String id;
    @NotBlank(message = "{gen.i18nMessage.messageKey.required}")
    @Size(max = 128, message = "{gen.i18nMessage.messageKey.maxLength}")
    private String messageKey;
    @NotBlank(message = "{gen.i18nMessage.locale.required}")
    @Size(max = 16, message = "{gen.i18nMessage.locale.maxLength}")
    private String locale;
    @NotBlank(message = "{gen.i18nMessage.messageValue.required}")
    @Size(max = 65535, message = "{gen.i18nMessage.messageValue.maxLength}")
    private String messageValue;
    @NotBlank(message = "{gen.i18nMessage.module.required}")
    @Size(max = 64, message = "{gen.i18nMessage.module.maxLength}")
    private String module;
    @NotBlank(message = "{gen.i18nMessage.scope.required}")
    @Size(max = 16, message = "{gen.i18nMessage.scope.maxLength}")
    private String scope;
    @Size(max = 256, message = "{gen.i18nMessage.remark.maxLength}")
    private String remark;
}

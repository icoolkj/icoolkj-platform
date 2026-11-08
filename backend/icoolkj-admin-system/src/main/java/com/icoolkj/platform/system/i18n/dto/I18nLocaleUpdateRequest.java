package com.icoolkj.platform.system.i18n.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 修改国际化语言配置表 */
@Data
public class I18nLocaleUpdateRequest {

    @NotBlank(message = "{validation.id.required}")
    private String id;
    @NotBlank(message = "{gen.i18nLocale.localeCode.required}")
    @Size(max = 16, message = "{gen.i18nLocale.localeCode.maxLength}")
    private String localeCode;
    @NotBlank(message = "{gen.i18nLocale.localeName.required}")
    @Size(max = 64, message = "{gen.i18nLocale.localeName.maxLength}")
    private String localeName;
    @NotBlank(message = "{gen.i18nLocale.nativeName.required}")
    @Size(max = 64, message = "{gen.i18nLocale.nativeName.maxLength}")
    private String nativeName;
    @NotNull(message = "{gen.i18nLocale.sortNo.required}")
    private Integer sortNo;
    @NotBlank(message = "{gen.i18nLocale.isDefault.required}")
    @Size(max = 1, message = "{gen.i18nLocale.isDefault.maxLength}")
    private String isDefault;
    @NotBlank(message = "{gen.i18nLocale.status.required}")
    @Size(max = 1, message = "{gen.i18nLocale.status.maxLength}")
    private String status;
    @Size(max = 256, message = "{gen.i18nLocale.remark.maxLength}")
    private String remark;
}

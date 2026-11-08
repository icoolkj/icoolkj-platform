package com.icoolkj.platform.system.config.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 新增系统参数表 */
@Data
public class ConfigCreateRequest {
    @NotBlank(message = "{gen.config.configName.required}")
    @Size(max = 100, message = "{gen.config.configName.maxLength}")
    private String configName;
    @NotBlank(message = "{gen.config.configKey.required}")
    @Size(max = 100, message = "{gen.config.configKey.maxLength}")
    private String configKey;
    @NotBlank(message = "{gen.config.configValue.required}")
    @Size(max = 65535, message = "{gen.config.configValue.maxLength}")
    private String configValue;
    @NotBlank(message = "{gen.config.configType.required}")
    @Size(max = 1, message = "{gen.config.configType.maxLength}")
    private String configType;
    @Size(max = 500, message = "{gen.config.remark.maxLength}")
    private String remark;
}

package com.icoolkj.platform.system.config.vo;

import lombok.Data;

/** Tenant-resolved config value (platform fallback applied). */
@Data
public class ConfigResolvedVO {
    private String configKey;
    private String configValue;
}

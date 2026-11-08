package com.icoolkj.platform.common.config;

/**
 * {@code sys_config} 键元数据：类型 / 是否可租户覆盖 / 是否公开 bootstrap / 是否敏感。
 * 产品新配置键必须在 {@link ConfigKeyRegistry} 登记后再使用。
 */
public record ConfigKeyMeta(
        String key,
        ConfigValueType valueType,
        boolean tenantOverridable,
        boolean publicBootstrap,
        boolean sensitive
) {
    public enum ConfigValueType {
        STRING,
        INT,
        BOOL,
        JSON
    }
}

package com.icoolkj.platform.system.tenant.service.impl;

import org.springframework.stereotype.Service;
import com.icoolkj.platform.system.config.service.ConfigService;

/**
 * Resolves effective configuration for a tenant.
 */
@Service
public class TenantConfigResolver {

    private final ConfigService configService;

    public TenantConfigResolver(ConfigService configService) {
        this.configService = configService;
    }

    public String resolveString(String tenantId, String configKey, String defaultValue) {
        return configService.getString(tenantId, configKey, defaultValue);
    }

    public int resolveInt(String tenantId, String configKey, int defaultValue) {
        return configService.getInt(tenantId, configKey, defaultValue);
    }

    public boolean resolveBoolean(String tenantId, String configKey, boolean defaultValue) {
        return configService.getBoolean(tenantId, configKey, defaultValue);
    }
}

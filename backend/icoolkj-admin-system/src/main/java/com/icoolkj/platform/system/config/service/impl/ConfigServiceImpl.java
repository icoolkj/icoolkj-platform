package com.icoolkj.platform.system.config.service.impl;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.framework.security.crypto.SensitiveConfigCrypto;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.system.config.domain.ConfigEntity;
import com.icoolkj.platform.system.config.mapper.ConfigMapper;
import com.icoolkj.platform.system.config.service.ConfigCacheService;
import com.icoolkj.platform.system.config.service.ConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ConfigServiceImpl implements ConfigService {

    private final ConfigMapper configMapper;
    private final ConfigCacheService configCacheService;
    private final SensitiveConfigCrypto sensitiveConfigCrypto;

    public ConfigServiceImpl(ConfigMapper configMapper,
                             ConfigCacheService configCacheService,
                             SensitiveConfigCrypto sensitiveConfigCrypto) {
        this.configMapper = configMapper;
        this.configCacheService = configCacheService;
        this.sensitiveConfigCrypto = sensitiveConfigCrypto;
    }

    @Override
    public String getString(String tenantId, String configKey, String defaultValue) {
        String cached = configCacheService.get(tenantId, configKey);
        if (StringUtils.hasText(cached)) {
            return cached;
        }

        String resolved = resolveRawValue(tenantId, configKey);
        if (!StringUtils.hasText(resolved)) {
            return defaultValue;
        }

        String plaintext = sensitiveConfigCrypto.open(configKey, resolved);
        configCacheService.put(tenantId, configKey, plaintext);
        return plaintext;
    }

    @Override
    public int getInt(String tenantId, String configKey, int defaultValue) {
        String value = getString(tenantId, configKey, null);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean(String tenantId, String configKey, boolean defaultValue) {
        String value = getString(tenantId, configKey, null);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    @Override
    public void evict(String tenantId, String configKey) {
        configCacheService.evict(tenantId, configKey);
    }

    private String resolveRawValue(String tenantId, String configKey) {
        if (StringUtils.hasText(tenantId)
                && !TenantConstants.PLATFORM_TENANT_ID.equals(tenantId)) {
            String tenantValue = loadValue(tenantId, configKey);
            if (StringUtils.hasText(tenantValue)) {
                return tenantValue;
            }
        }
        return loadValue(TenantConstants.PLATFORM_TENANT_ID, configKey);
    }

    private String loadValue(String tenantId, String configKey) {
        ConfigEntity entity = TenantScopeRunner.runIgnoringTenant(
                () -> configMapper.selectByTenantAndKey(tenantId, configKey));
        return entity == null ? null : entity.getConfigValue();
    }
}

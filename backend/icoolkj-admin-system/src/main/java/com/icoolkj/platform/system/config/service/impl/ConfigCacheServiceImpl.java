package com.icoolkj.platform.system.config.service.impl;

import com.icoolkj.platform.common.constant.CacheKeyConstants;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.framework.cache.CacheTtlProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.icoolkj.platform.framework.cache.CacheService;
import com.icoolkj.platform.system.config.service.ConfigCacheService;

import java.time.Duration;
import java.util.Set;

@Service
public class ConfigCacheServiceImpl implements ConfigCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final CacheTtlProperties cacheTtlProperties;

    public ConfigCacheServiceImpl(StringRedisTemplate stringRedisTemplate,
                                  CacheTtlProperties cacheTtlProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheTtlProperties = cacheTtlProperties;
    }

    @Override
    public String get(String tenantId, String configKey) {
        return stringRedisTemplate.opsForValue().get(buildKey(tenantId, configKey));
    }

    @Override
    public void put(String tenantId, String configKey, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        stringRedisTemplate.opsForValue().set(
                buildKey(tenantId, configKey),
                value,
                configTtl()
        );
    }

    @Override
    public void evict(String tenantId, String configKey) {
        stringRedisTemplate.delete(buildKey(tenantId, configKey));
    }

    @Override
    public void refreshTenant(String tenantId) {
        String normalizedTenantId = StringUtils.hasText(tenantId) ? tenantId : TenantConstants.PLATFORM_TENANT_ID;
        String pattern = cacheTtlProperties.getKeyPrefix() + ":"
                + CacheKeyConstants.TENANT_CONFIG
                + normalizedTenantId + ":*";
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    @Override
    public void refreshAll() {
        String pattern = cacheTtlProperties.getKeyPrefix() + ":" + CacheKeyConstants.TENANT_CONFIG + "*";
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    private String buildKey(String tenantId, String configKey) {
        String normalizedTenantId = StringUtils.hasText(tenantId) ? tenantId : TenantConstants.PLATFORM_TENANT_ID;
        return cacheTtlProperties.getKeyPrefix() + ":"
                + CacheKeyConstants.TENANT_CONFIG
                + normalizedTenantId + ":" + configKey;
    }

    private Duration configTtl() {
        return Duration.ofSeconds(cacheTtlProperties.getTtl().getConfig());
    }
}

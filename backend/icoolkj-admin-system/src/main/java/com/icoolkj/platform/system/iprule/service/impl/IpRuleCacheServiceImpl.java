package com.icoolkj.platform.system.iprule.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.constant.CacheKeyConstants;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.framework.cache.CacheTtlProperties;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.icoolkj.platform.framework.cache.CacheService;
import com.icoolkj.platform.system.iprule.domain.IpAccessRuleEntity;
import com.icoolkj.platform.system.iprule.mapper.IpAccessRuleMapper;
import com.icoolkj.platform.system.iprule.service.IpRuleCacheService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class IpRuleCacheServiceImpl implements IpRuleCacheService {

    private static final TypeReference<List<IpAccessRuleEntity>> RULE_LIST_TYPE = new TypeReference<>() {
    };

    private final IpAccessRuleMapper ipAccessRuleMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final CacheTtlProperties cacheTtlProperties;
    private final ObjectMapper objectMapper;

    public IpRuleCacheServiceImpl(IpAccessRuleMapper ipAccessRuleMapper,
                                  StringRedisTemplate stringRedisTemplate,
                                  CacheTtlProperties cacheTtlProperties,
                                  ObjectMapper objectMapper) {
        this.ipAccessRuleMapper = ipAccessRuleMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheTtlProperties = cacheTtlProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<IpAccessRuleEntity> getActiveRules(String tenantId) {
        String cacheKey = buildKey(tenantId);
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, RULE_LIST_TYPE);
            } catch (Exception ex) {
                stringRedisTemplate.delete(cacheKey);
            }
        }

        List<IpAccessRuleEntity> rules = loadActiveRules(tenantId);
        try {
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(rules),
                    ruleTtl()
            );
        } catch (Exception ignored) {
            // Cache write failure should not block login.
        }
        return rules;
    }

    @Override
    public void evict(String tenantId) {
        stringRedisTemplate.delete(buildKey(tenantId));
    }

    private List<IpAccessRuleEntity> loadActiveRules(String tenantId) {
        List<IpAccessRuleEntity> loaded = TenantScopeRunner.runIgnoringTenant(
                () -> ipAccessRuleMapper.selectActiveRules(
                        TenantConstants.PLATFORM_TENANT_ID,
                        tenantId,
                        LocalDateTime.now()));
        if (loaded == null || loaded.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(loaded);
    }

    private String buildKey(String tenantId) {
        String normalizedTenantId = StringUtils.hasText(tenantId) ? tenantId : TenantConstants.PLATFORM_TENANT_ID;
        return cacheTtlProperties.getKeyPrefix() + ":"
                + CacheKeyConstants.TENANT_IP_RULE
                + normalizedTenantId;
    }

    private Duration ruleTtl() {
        return Duration.ofSeconds(cacheTtlProperties.getTtl().getTenantIpRule());
    }
}

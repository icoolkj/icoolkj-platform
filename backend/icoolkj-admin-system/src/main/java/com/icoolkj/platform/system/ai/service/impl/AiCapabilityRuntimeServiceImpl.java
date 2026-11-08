package com.icoolkj.platform.system.ai.service.impl;

import com.icoolkj.platform.common.constant.AiCapability;
import com.icoolkj.platform.common.constant.ConfigKeys;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.security.crypto.SensitiveConfigCrypto;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.system.ai.domain.AiCapabilityBindingEntity;
import com.icoolkj.platform.system.ai.domain.AiModelEntity;
import com.icoolkj.platform.system.ai.domain.AiProviderEntity;
import com.icoolkj.platform.system.ai.mapper.AiCatalogMapper;
import com.icoolkj.platform.system.ai.protocol.AiProtocols;
import com.icoolkj.platform.system.ai.service.AiCapabilityRuntimeService;
import com.icoolkj.platform.system.ai.support.AiResolvedRuntime;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiCapabilityRuntimeServiceImpl implements AiCapabilityRuntimeService {

    private final AiCatalogMapper catalogMapper;
    private final SensitiveConfigCrypto sensitiveConfigCrypto;

    public AiCapabilityRuntimeServiceImpl(
            AiCatalogMapper catalogMapper,
            SensitiveConfigCrypto sensitiveConfigCrypto) {
        this.catalogMapper = catalogMapper;
        this.sensitiveConfigCrypto = sensitiveConfigCrypto;
    }

    @Override
    public AiResolvedRuntime resolve(String capability) {
        return resolve(null, capability);
    }

    @Override
    public AiResolvedRuntime resolve(String tenantId, String capability) {
        if (!StringUtils.hasText(capability)) {
            throw new BusinessException("error.ai.capability_required");
        }
        String cap = capability.trim().toLowerCase();
        String tid = StringUtils.hasText(tenantId) ? tenantId.trim() : resolveCurrentTenantId();

        return TenantScopeRunner.runIgnoringTenant(() -> {
            AiResolvedRuntime fromCatalog = resolveFromCatalog(tid, cap);
            if (fromCatalog != null) {
                return fromCatalog;
            }
            throw new BusinessException("error.ai.capability_not_configured", cap);
        });
    }

    private AiResolvedRuntime resolveFromCatalog(String tenantId, String capability) {
        AiCapabilityBindingEntity binding = catalogMapper.selectBinding(tenantId, capability);
        if (binding != null && binding.getEnabled() != null && binding.getEnabled() == 0) {
            return disabledRuntime(capability);
        }
        if (binding == null || binding.getEnabled() == null || binding.getEnabled() != 1) {
            if (!TenantConstants.PLATFORM_TENANT_ID.equals(tenantId)) {
                binding = catalogMapper.selectBinding(TenantConstants.PLATFORM_TENANT_ID, capability);
            }
        }
        if (binding == null || binding.getEnabled() == null || binding.getEnabled() != 1) {
            return null;
        }
        AiModelEntity model = catalogMapper.selectModelById(binding.getModelId());
        if (model == null || !"0".equals(model.getStatus())) {
            return null;
        }
        if (!hasCapability(model.getCapabilities(), capability)) {
            return null;
        }
        AiProviderEntity provider = catalogMapper.selectProviderById(model.getProviderId());
        if (provider == null || !"0".equals(provider.getStatus())) {
            return null;
        }
        String apiKey = sensitiveConfigCrypto.open(ConfigKeys.AI_PROVIDER_API_KEY, provider.getApiKeyCipher());
        boolean enabled = StringUtils.hasText(apiKey) && StringUtils.hasText(provider.getBaseUrl());
        int timeout = provider.getTimeoutSeconds() == null ? 60 : provider.getTimeoutSeconds();
        String protocol = AiProtocols.normalize(provider.getProtocol());
        return new AiResolvedRuntime(
                enabled,
                provider.getBaseUrl(),
                model.getModelCode(),
                apiKey == null ? "" : apiKey,
                timeout,
                provider.getId(),
                provider.getProviderName(),
                protocol,
                model.getId(),
                capability,
                model.getDefaultParamsJson());
    }

    private static AiResolvedRuntime disabledRuntime(String capability) {
        return new AiResolvedRuntime(false, "", "", "", 60, null, null, null, null, capability, null);
    }

    private static String resolveCurrentTenantId() {
        String tid = SecurityContextHelper.getTenantId();
        if (!StringUtils.hasText(tid)) {
            tid = TenantContextHolder.getTenantId();
        }
        return StringUtils.hasText(tid) ? tid : TenantConstants.PLATFORM_TENANT_ID;
    }

    private static boolean hasCapability(String capabilities, String capability) {
        if (!StringUtils.hasText(capabilities) || !StringUtils.hasText(capability)) {
            return false;
        }
        for (String part : capabilities.split(",")) {
            if (capability.equalsIgnoreCase(part.trim())) {
                return true;
            }
        }
        return false;
    }
}

package com.icoolkj.platform.system.ai.service.impl;

import com.icoolkj.platform.common.constant.AiCapability;
import com.icoolkj.platform.common.constant.ConfigKeys;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.security.crypto.SensitiveConfigCrypto;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.system.ai.domain.AiCapabilityBindingEntity;
import com.icoolkj.platform.system.ai.domain.AiModelEntity;
import com.icoolkj.platform.system.ai.domain.AiProviderEntity;
import com.icoolkj.platform.system.ai.dto.AiCapabilityBindRequest;
import com.icoolkj.platform.system.ai.dto.AiModelSaveRequest;
import com.icoolkj.platform.system.ai.dto.AiProviderSaveRequest;
import com.icoolkj.platform.system.ai.client.OpenAiCompatibleChatClient;
import com.icoolkj.platform.system.ai.client.OpenAiCompatibleImageClient;
import com.icoolkj.platform.system.ai.mapper.AiCatalogMapper;
import com.icoolkj.platform.system.ai.service.AiCapabilityRuntimeService;
import com.icoolkj.platform.system.ai.service.AiCatalogService;
import com.icoolkj.platform.system.ai.support.AiResolvedRuntime;
import com.icoolkj.platform.system.ai.protocol.AiCapabilityProbe;
import com.icoolkj.platform.system.ai.protocol.AiProtocols;
import com.icoolkj.platform.system.ai.protocol.openai.OpenAiCompatibleUrls;
import com.icoolkj.platform.system.ai.vo.AiCapabilityBindingVO;
import com.icoolkj.platform.system.ai.vo.AiModelVO;
import com.icoolkj.platform.system.ai.vo.AiProviderVO;
import com.icoolkj.platform.system.ai.vo.AiRuntimeStatusVO;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AiCatalogServiceImpl implements AiCatalogService {

    private final AiCatalogMapper catalogMapper;
    private final SensitiveConfigCrypto sensitiveConfigCrypto;
    private final AiCapabilityRuntimeService capabilityRuntimeService;
    private final OpenAiCompatibleChatClient chatClient;
    private final OpenAiCompatibleImageClient imageClient;

    public AiCatalogServiceImpl(
            AiCatalogMapper catalogMapper,
            SensitiveConfigCrypto sensitiveConfigCrypto,
            AiCapabilityRuntimeService capabilityRuntimeService,
            OpenAiCompatibleChatClient chatClient,
            OpenAiCompatibleImageClient imageClient) {
        this.catalogMapper = catalogMapper;
        this.sensitiveConfigCrypto = sensitiveConfigCrypto;
        this.capabilityRuntimeService = capabilityRuntimeService;
        this.chatClient = chatClient;
        this.imageClient = imageClient;
    }

    @Override
    public List<AiProviderVO> listProviders() {
        String tid = currentTenantId();
        return TenantScopeRunner.runIgnoringTenant(() -> {
            List<AiProviderVO> list = new ArrayList<>();
            if (!TenantConstants.PLATFORM_TENANT_ID.equals(tid)) {
                for (AiProviderEntity p : catalogMapper.selectProviders(tid)) {
                    list.add(toProviderVo(p));
                }
                for (AiProviderEntity p : catalogMapper.selectProviders(TenantConstants.PLATFORM_TENANT_ID)) {
                    if (catalogMapper.selectProviderByCode(tid, p.getProviderCode()) == null) {
                        list.add(toProviderVo(p));
                    }
                }
            } else {
                for (AiProviderEntity p : catalogMapper.selectProviders(TenantConstants.PLATFORM_TENANT_ID)) {
                    list.add(toProviderVo(p));
                }
            }
            return list;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveProvider(AiProviderSaveRequest request) {
        String tid = currentTenantId();
        return TenantScopeRunner.runIgnoringTenant(() -> {
            if (StringUtils.hasText(request.getId())) {
                AiProviderEntity existing = requireOwnedProvider(request.getId(), tid);
                existing.setProviderName(request.getProviderName().trim());
                existing.setProtocol(AiProtocols.normalize(request.getProtocol()));
                AiProtocols.requireSupported(existing.getProtocol());
                existing.setBaseUrl(normalizeProviderBaseUrl(request.getBaseUrl()));
                if (StringUtils.hasText(request.getApiKey())) {
                    existing.setApiKeyCipher(sensitiveConfigCrypto.sealIfSensitive(
                            ConfigKeys.AI_PROVIDER_API_KEY, request.getApiKey().trim()));
                }
                existing.setTimeoutSeconds(request.getTimeoutSeconds() == null ? 60 : request.getTimeoutSeconds());
                existing.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "0");
                existing.setRemark(request.getRemark());
                AuditFieldHelper.fillUpdate(existing);
                catalogMapper.updateProvider(existing);
                return existing.getId();
            }
            String code = request.getProviderCode().trim();
            if (catalogMapper.selectProviderByCode(tid, code) != null) {
                throw new BusinessException("error.ai.provider_code_exists");
            }
            AiProviderEntity entity = new AiProviderEntity();
            entity.setId(IdGeneratorUtils.nextIdStr());
            entity.setTenantId(tid);
            entity.setProviderCode(code);
            entity.setProviderName(request.getProviderName().trim());
            entity.setProtocol(AiProtocols.normalize(request.getProtocol()));
            AiProtocols.requireSupported(entity.getProtocol());
            entity.setBaseUrl(normalizeProviderBaseUrl(request.getBaseUrl()));
            if (StringUtils.hasText(request.getApiKey())) {
                entity.setApiKeyCipher(sensitiveConfigCrypto.sealIfSensitive(
                        ConfigKeys.AI_PROVIDER_API_KEY, request.getApiKey().trim()));
            }
            entity.setTimeoutSeconds(request.getTimeoutSeconds() == null ? 60 : request.getTimeoutSeconds());
            entity.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "0");
            entity.setRemark(request.getRemark());
            AuditFieldHelper.fillInsert(entity);
            catalogMapper.insertProvider(entity);
            return entity.getId();
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProvider(String id) {
        String tid = currentTenantId();
        TenantScopeRunner.runIgnoringTenant(() -> {
            requireOwnedProvider(id, tid);
            catalogMapper.purgeBindingsByProvider(id);
            catalogMapper.purgeModelsByProvider(id);
            catalogMapper.purgeProvider(id);
            return null;
        });
    }

    @Override
    public List<AiModelVO> listSelectableModels() {
        String tid = currentTenantId();
        return TenantScopeRunner.runIgnoringTenant(() -> {
            java.util.Set<String> adoptedCodes = new java.util.HashSet<>();
            if (!TenantConstants.PLATFORM_TENANT_ID.equals(tid)) {
                for (AiProviderEntity p : catalogMapper.selectProviders(tid)) {
                    adoptedCodes.add(p.getProviderCode());
                }
            }
            List<AiModelVO> list = new ArrayList<>();
            for (AiModelEntity m : catalogMapper.selectSelectableModels(tid)) {
                if (!TenantConstants.PLATFORM_TENANT_ID.equals(tid)
                        && TenantConstants.PLATFORM_TENANT_ID.equals(m.getTenantId())
                        && !adoptedCodes.isEmpty()) {
                    AiProviderEntity pp = catalogMapper.selectProviderById(m.getProviderId());
                    if (pp != null && adoptedCodes.contains(pp.getProviderCode())) {
                        continue;
                    }
                }
                list.add(toModelVo(m));
            }
            return list;
        });
    }

    @Override
    public List<AiModelVO> listOwnedModels() {
        String tid = currentTenantId();
        return TenantScopeRunner.runIgnoringTenant(() -> {
            List<AiModelVO> list = new ArrayList<>();
            for (AiModelEntity m : catalogMapper.selectModelsByTenant(tid)) {
                list.add(toModelVo(m));
            }
            return list;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveModel(AiModelSaveRequest request) {
        String tid = currentTenantId();
        return TenantScopeRunner.runIgnoringTenant(() -> {
            AiProviderEntity provider = requireOwnedProvider(request.getProviderId(), tid);
            if (StringUtils.hasText(request.getId())) {
                AiModelEntity existing = requireOwnedModel(request.getId(), tid);
                existing.setModelCode(request.getModelCode().trim());
                existing.setModelName(request.getModelName().trim());
                existing.setCapabilities(normalizeCapabilities(request.getCapabilities()));
                existing.setDefaultParamsJson(request.getDefaultParamsJson());
                existing.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "0");
                existing.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
                existing.setRemark(request.getRemark());
                AuditFieldHelper.fillUpdate(existing);
                catalogMapper.updateModel(existing);
                return existing.getId();
            }
            AiModelEntity entity = new AiModelEntity();
            entity.setId(IdGeneratorUtils.nextIdStr());
            entity.setTenantId(provider.getTenantId());
            entity.setProviderId(provider.getId());
            entity.setModelCode(request.getModelCode().trim());
            entity.setModelName(request.getModelName().trim());
            entity.setCapabilities(normalizeCapabilities(request.getCapabilities()));
            entity.setDefaultParamsJson(request.getDefaultParamsJson());
            entity.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "0");
            entity.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
            entity.setRemark(request.getRemark());
            AuditFieldHelper.fillInsert(entity);
            catalogMapper.insertModel(entity);
            return entity.getId();
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(String id) {
        String tid = currentTenantId();
        TenantScopeRunner.runIgnoringTenant(() -> {
            requireOwnedModel(id, tid);
            catalogMapper.purgeBindingsByModel(id);
            catalogMapper.purgeModel(id);
            return null;
        });
    }

    @Override
    public List<AiCapabilityBindingVO> listEffectiveBindings() {
        String tid = currentTenantId();
        return TenantScopeRunner.runIgnoringTenant(() -> {
            List<AiCapabilityBindingVO> list = new ArrayList<>();
            for (String cap : List.of(AiCapability.CHAT, AiCapability.IMAGE)) {
                list.add(buildEffectiveBinding(tid, cap));
            }
            return list;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindCapability(AiCapabilityBindRequest request) {
        String tid = currentTenantId();
        String capability = request.getCapability().trim().toLowerCase(Locale.ROOT);
        if (!AiCapability.isImplemented(capability)) {
            throw new BusinessException("error.ai.capability_not_implemented", capability);
        }
        TenantScopeRunner.runIgnoringTenant(() -> {
            if (!StringUtils.hasText(request.getModelId())) {
                if (TenantConstants.PLATFORM_TENANT_ID.equals(tid)) {
                    throw new BusinessException("error.ai.platform_binding_required");
                }
                AiCapabilityBindingEntity existing = catalogMapper.selectBinding(tid, capability);
                if (existing != null) {
                    catalogMapper.purgeBinding(existing.getId());
                }
                return null;
            }
            AiModelEntity model = catalogMapper.selectModelById(request.getModelId().trim());
            if (model == null || !"0".equals(model.getStatus())) {
                throw new BusinessException("error.ai.model_not_found");
            }
            if (!TenantConstants.PLATFORM_TENANT_ID.equals(model.getTenantId())
                    && !tid.equals(model.getTenantId())) {
                throw new BusinessException("error.ai.model_not_selectable");
            }
            if (!capabilityMatches(model.getCapabilities(), capability)) {
                throw new BusinessException("error.ai.model_capability_mismatch");
            }
            AiCapabilityBindingEntity existing = catalogMapper.selectBinding(tid, capability);
            if (existing == null) {
                AiCapabilityBindingEntity entity = new AiCapabilityBindingEntity();
                entity.setId(IdGeneratorUtils.nextIdStr());
                entity.setTenantId(tid);
                entity.setCapability(capability);
                entity.setModelId(model.getId());
                entity.setEnabled(1);
                AuditFieldHelper.fillInsert(entity);
                catalogMapper.insertBinding(entity);
            } else {
                existing.setModelId(model.getId());
                existing.setEnabled(1);
                AuditFieldHelper.fillUpdate(existing);
                catalogMapper.updateBinding(existing);
            }
            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String adoptPlatformProvider(String platformProviderId, String apiKey) {
        String tid = currentTenantId();
        if (TenantConstants.PLATFORM_TENANT_ID.equals(tid)) {
            throw new BusinessException("error.ai.platform_use_edit_provider");
        }
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException("error.ai.api_key_missing");
        }
        return TenantScopeRunner.runIgnoringTenant(() -> {
            AiProviderEntity platform = catalogMapper.selectProviderById(platformProviderId);
            if (platform == null || !TenantConstants.PLATFORM_TENANT_ID.equals(platform.getTenantId())) {
                throw new BusinessException("error.ai.provider_not_found");
            }
            String sealedKey = sensitiveConfigCrypto.sealIfSensitive(
                    ConfigKeys.AI_PROVIDER_API_KEY, apiKey.trim());
            AiProviderEntity tenantProvider = catalogMapper.selectProviderByCode(tid, platform.getProviderCode());
            if (tenantProvider == null) {
                tenantProvider = new AiProviderEntity();
                tenantProvider.setId(IdGeneratorUtils.nextIdStr());
                tenantProvider.setTenantId(tid);
                tenantProvider.setProviderCode(platform.getProviderCode());
                tenantProvider.setProviderName(platform.getProviderName());
                tenantProvider.setProtocol(platform.getProtocol());
                tenantProvider.setBaseUrl(platform.getBaseUrl());
                tenantProvider.setApiKeyCipher(sealedKey);
                tenantProvider.setTimeoutSeconds(
                        platform.getTimeoutSeconds() == null ? 60 : platform.getTimeoutSeconds());
                tenantProvider.setStatus("0");
                tenantProvider.setRemark("Adopted from platform provider");
                AuditFieldHelper.fillInsert(tenantProvider);
                catalogMapper.insertProvider(tenantProvider);
            } else {
                tenantProvider.setBaseUrl(platform.getBaseUrl());
                tenantProvider.setProtocol(platform.getProtocol());
                tenantProvider.setTimeoutSeconds(
                        platform.getTimeoutSeconds() == null ? 60 : platform.getTimeoutSeconds());
                tenantProvider.setApiKeyCipher(sealedKey);
                tenantProvider.setStatus("0");
                AuditFieldHelper.fillUpdate(tenantProvider);
                catalogMapper.updateProvider(tenantProvider);
            }

            List<AiModelEntity> platformModels = catalogMapper.selectModelsByProvider(platform.getId());
            Map<String, String> firstModelByCapability = new LinkedHashMap<>();
            if (platformModels != null) {
                for (AiModelEntity pm : platformModels) {
                    if (!"0".equals(pm.getStatus())) {
                        continue;
                    }
                    AiModelEntity tm = findTenantModel(tid, tenantProvider.getId(), pm.getModelCode());
                    if (tm == null) {
                        tm = new AiModelEntity();
                        tm.setId(IdGeneratorUtils.nextIdStr());
                        tm.setTenantId(tid);
                        tm.setProviderId(tenantProvider.getId());
                        tm.setModelCode(pm.getModelCode());
                        tm.setModelName(pm.getModelName());
                        tm.setCapabilities(pm.getCapabilities());
                        tm.setDefaultParamsJson(pm.getDefaultParamsJson());
                        tm.setStatus("0");
                        tm.setSortNo(pm.getSortNo() == null ? 0 : pm.getSortNo());
                        tm.setRemark("Adopted from platform model");
                        AuditFieldHelper.fillInsert(tm);
                        catalogMapper.insertModel(tm);
                    }
                    for (String cap : List.of(AiCapability.CHAT, AiCapability.IMAGE)) {
                        if (firstModelByCapability.containsKey(cap)) {
                            continue;
                        }
                        if (capabilityMatches(tm.getCapabilities(), cap)) {
                            firstModelByCapability.put(cap, tm.getId());
                        }
                    }
                }
            }
            for (Map.Entry<String, String> entry : firstModelByCapability.entrySet()) {
                upsertTenantBinding(tid, entry.getKey(), entry.getValue());
            }
            return tenantProvider.getId();
        });
    }

    private AiModelEntity findTenantModel(String tenantId, String providerId, String modelCode) {
        List<AiModelEntity> models = catalogMapper.selectModelsByProvider(providerId);
        if (models == null) {
            return null;
        }
        for (AiModelEntity m : models) {
            if (tenantId.equals(m.getTenantId()) && modelCode.equals(m.getModelCode())) {
                return m;
            }
        }
        return null;
    }

    private void upsertTenantBinding(String tenantId, String capability, String modelId) {
        AiCapabilityBindingEntity existing = catalogMapper.selectBinding(tenantId, capability);
        if (existing == null) {
            AiCapabilityBindingEntity entity = new AiCapabilityBindingEntity();
            entity.setId(IdGeneratorUtils.nextIdStr());
            entity.setTenantId(tenantId);
            entity.setCapability(capability);
            entity.setModelId(modelId);
            entity.setEnabled(1);
            AuditFieldHelper.fillInsert(entity);
            catalogMapper.insertBinding(entity);
        } else {
            existing.setModelId(modelId);
            existing.setEnabled(1);
            AuditFieldHelper.fillUpdate(existing);
            catalogMapper.updateBinding(existing);
        }
    }

    @Override
    public AiRuntimeStatusVO runtimeStatus(String capability) {
        String cap = normalizeCapability(capability);
        AiCapabilityBindingVO binding = buildEffectiveBinding(currentTenantId(), cap);
        AiRuntimeStatusVO vo = new AiRuntimeStatusVO();
        vo.setCapability(cap);
        vo.setUsingPlatformDefault(binding.isUsingPlatformDefault());
        vo.setModelCode(binding.getModelCode());
        vo.setModelName(binding.getModelName());
        vo.setProviderName(binding.getProviderName());
        try {
            AiResolvedRuntime runtime = capabilityRuntimeService.resolve(cap);
            vo.setEnabled(runtime.isEnabled());
            vo.setApiKeyConfigured(StringUtils.hasText(runtime.getApiKey()));
            vo.setBaseUrl(runtime.getBaseUrl());
            vo.setProtocol(runtime.getProtocol());
            vo.setTimeoutSeconds(runtime.getTimeoutSeconds());
            vo.setProviderName(StringUtils.hasText(vo.getProviderName())
                    ? vo.getProviderName()
                    : runtime.getProviderName());
            if (!StringUtils.hasText(vo.getModelCode())) {
                vo.setModelCode(runtime.getModel());
            }
        } catch (BusinessException ex) {
            vo.setEnabled(false);
            vo.setApiKeyConfigured(false);
        }
        return vo;
    }

    @Override
    public String testCapability(String capability) {
        String cap = normalizeCapability(capability);
        if (!AiCapability.isImplemented(cap)) {
            throw new BusinessException("error.ai.capability_not_implemented", cap);
        }
        AiResolvedRuntime runtime = capabilityRuntimeService.resolve(cap);
        if (!runtime.isEnabled()) {
            throw new BusinessException("error.ai.not_enabled");
        }
        if (!StringUtils.hasText(runtime.getApiKey())) {
            throw new BusinessException("error.ai.api_key_missing");
        }
        if (AiCapability.CHAT.equals(cap)) {
            return chatClient.chat(
                    runtime,
                    AiCapabilityProbe.CHAT_SYSTEM,
                    AiCapabilityProbe.CHAT_USER);
        }
        if (AiCapability.IMAGE.equals(cap)) {
            imageClient.generateAll(runtime, AiCapabilityProbe.IMAGE_PROMPT, null);
            return "OK";
        }
        throw new BusinessException("error.ai.capability_test_unsupported", cap);
    }

    private AiCapabilityBindingVO buildEffectiveBinding(String tenantId, String capability) {
        AiCapabilityBindingVO vo = new AiCapabilityBindingVO();
        vo.setCapability(capability);
        AiCapabilityBindingEntity tenantBind = catalogMapper.selectBinding(tenantId, capability);
        if (tenantBind != null && tenantBind.getEnabled() != null && tenantBind.getEnabled() == 0) {
            vo.setId(tenantBind.getId());
            vo.setTenantId(tenantBind.getTenantId());
            vo.setEnabled(0);
            vo.setUsingPlatformDefault(false);
            return vo;
        }
        AiCapabilityBindingEntity effective = tenantBind;
        boolean usingPlatform = false;
        if (effective == null || effective.getEnabled() == null || effective.getEnabled() != 1) {
            effective = catalogMapper.selectBinding(TenantConstants.PLATFORM_TENANT_ID, capability);
            usingPlatform = true;
        }
        vo.setUsingPlatformDefault(usingPlatform || TenantConstants.PLATFORM_TENANT_ID.equals(tenantId));
        if (effective == null) {
            return vo;
        }
        vo.setId(effective.getId());
        vo.setTenantId(effective.getTenantId());
        vo.setModelId(effective.getModelId());
        vo.setEnabled(effective.getEnabled());
        AiModelEntity model = catalogMapper.selectModelById(effective.getModelId());
        if (model != null) {
            vo.setModelCode(model.getModelCode());
            vo.setModelName(model.getModelName());
            AiProviderEntity provider = catalogMapper.selectProviderById(model.getProviderId());
            if (provider != null) {
                vo.setProviderName(provider.getProviderName());
            }
        }
        return vo;
    }

    private static String normalizeCapability(String capability) {
        if (!StringUtils.hasText(capability)) {
            throw new BusinessException("error.ai.capability_required");
        }
        String cap = capability.trim().toLowerCase(Locale.ROOT);
        if (!AiCapability.isImplemented(cap)) {
            throw new BusinessException("error.ai.capability_not_implemented", cap);
        }
        return cap;
    }

    private AiProviderEntity requireOwnedProvider(String id, String tenantId) {
        AiProviderEntity entity = catalogMapper.selectProviderById(id);
        if (entity == null) {
            throw new BusinessException("error.ai.provider_not_found");
        }
        if (!tenantId.equals(entity.getTenantId())) {
            throw new BusinessException("error.ai.provider_not_owned");
        }
        return entity;
    }

    private AiModelEntity requireOwnedModel(String id, String tenantId) {
        AiModelEntity entity = catalogMapper.selectModelById(id);
        if (entity == null) {
            throw new BusinessException("error.ai.model_not_found");
        }
        if (!tenantId.equals(entity.getTenantId())) {
            throw new BusinessException("error.ai.model_not_owned");
        }
        return entity;
    }

    private AiProviderVO toProviderVo(AiProviderEntity entity) {
        AiProviderVO vo = new AiProviderVO();
        vo.setId(entity.getId());
        vo.setTenantId(entity.getTenantId());
        vo.setProviderCode(entity.getProviderCode());
        vo.setProviderName(entity.getProviderName());
        vo.setProtocol(entity.getProtocol());
        vo.setBaseUrl(entity.getBaseUrl());
        String raw = sensitiveConfigCrypto.open(ConfigKeys.AI_PROVIDER_API_KEY, entity.getApiKeyCipher());
        vo.setApiKeyConfigured(StringUtils.hasText(raw));
        vo.setApiKeyMasked(SensitiveConfigCrypto.mask(raw));
        vo.setTimeoutSeconds(entity.getTimeoutSeconds());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setPlatformOwned(TenantConstants.PLATFORM_TENANT_ID.equals(entity.getTenantId()));
        return vo;
    }

    private AiModelVO toModelVo(AiModelEntity entity) {
        AiModelVO vo = new AiModelVO();
        vo.setId(entity.getId());
        vo.setTenantId(entity.getTenantId());
        vo.setProviderId(entity.getProviderId());
        vo.setModelCode(entity.getModelCode());
        vo.setModelName(entity.getModelName());
        vo.setCapabilities(entity.getCapabilities());
        vo.setDefaultParamsJson(entity.getDefaultParamsJson());
        vo.setStatus(entity.getStatus());
        vo.setSortNo(entity.getSortNo());
        vo.setRemark(entity.getRemark());
        vo.setPlatformOwned(TenantConstants.PLATFORM_TENANT_ID.equals(entity.getTenantId()));
        AiProviderEntity provider = catalogMapper.selectProviderById(entity.getProviderId());
        if (provider != null) {
            vo.setProviderName(provider.getProviderName());
        }
        return vo;
    }

    private static String normalizeCapabilities(String raw) {
        String[] parts = raw.split(",");
        List<String> cleaned = new ArrayList<>();
        for (String p : parts) {
            if (StringUtils.hasText(p)) {
                cleaned.add(p.trim().toLowerCase(Locale.ROOT));
            }
        }
        if (cleaned.isEmpty()) {
            throw new BusinessException("error.ai.capabilities_required");
        }
        return String.join(",", cleaned);
    }

    private static boolean capabilityMatches(String capabilities, String capability) {
        if (!StringUtils.hasText(capabilities)) {
            return false;
        }
        for (String p : capabilities.split(",")) {
            if (capability.equalsIgnoreCase(p.trim())) {
                return true;
            }
        }
        return false;
    }

    private static String currentTenantId() {
        String tid = SecurityContextHelper.getTenantId();
        if (!StringUtils.hasText(tid)) {
            tid = TenantContextHolder.getTenantId();
        }
        return StringUtils.hasText(tid) ? tid : TenantConstants.PLATFORM_TENANT_ID;
    }

    private static String normalizeProviderBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            throw new BusinessException("error.ai.base_url_required");
        }
        return OpenAiCompatibleUrls.requireApiRoot(baseUrl);
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

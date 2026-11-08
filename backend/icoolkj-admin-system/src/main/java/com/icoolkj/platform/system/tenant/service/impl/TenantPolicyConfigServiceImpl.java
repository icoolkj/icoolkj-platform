package com.icoolkj.platform.system.tenant.service.impl;

import com.icoolkj.platform.common.config.ConfigKeyMeta;
import com.icoolkj.platform.common.config.ConfigKeyRegistry;
import com.icoolkj.platform.common.constant.ConfigKeys;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.security.crypto.SensitiveConfigCrypto;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.system.config.domain.ConfigEntity;
import com.icoolkj.platform.system.tenant.dto.TenantPolicyConfigUpdateRequest;
import com.icoolkj.platform.system.config.mapper.ConfigMapper;
import com.icoolkj.platform.system.config.service.ConfigCacheService;
import com.icoolkj.platform.system.config.service.ConfigService;
import com.icoolkj.platform.system.user.service.PasswordPolicyService;
import com.icoolkj.platform.system.tenant.service.TenantPolicyConfigService;
import com.icoolkj.platform.system.tenant.vo.TenantPolicyConfigItemVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class TenantPolicyConfigServiceImpl implements TenantPolicyConfigService {

    private final ConfigMapper configMapper;
    private final ConfigService configService;
    private final ConfigCacheService configCacheService;
    private final PasswordPolicyService passwordPolicyService;
    private final SensitiveConfigCrypto sensitiveConfigCrypto;

    public TenantPolicyConfigServiceImpl(ConfigMapper configMapper,
                                         ConfigService configService,
                                         ConfigCacheService configCacheService,
                                         PasswordPolicyService passwordPolicyService,
                                         SensitiveConfigCrypto sensitiveConfigCrypto) {
        this.configMapper = configMapper;
        this.configService = configService;
        this.configCacheService = configCacheService;
        this.passwordPolicyService = passwordPolicyService;
        this.sensitiveConfigCrypto = sensitiveConfigCrypto;
    }

    @Override
    public List<TenantPolicyConfigItemVO> catalog() {
        String tenantId = requireTenantId();
        List<TenantPolicyConfigItemVO> items = new ArrayList<>();
        for (ConfigKeyMeta meta : ConfigKeyRegistry.tenantOverridableMetas()) {
            TenantPolicyConfigItemVO item = new TenantPolicyConfigItemVO();
            item.setConfigKey(meta.key());
            item.setValueType(meta.valueType().name());
            item.setSensitive(meta.sensitive());
            String platformPlain = sensitiveConfigCrypto.open(meta.key(), loadRaw(TenantConstants.PLATFORM_TENANT_ID, meta.key()));
            String tenantPlain = TenantConstants.PLATFORM_TENANT_ID.equals(tenantId)
                    ? null
                    : sensitiveConfigCrypto.open(meta.key(), loadRaw(tenantId, meta.key()));
            if (meta.sensitive()) {
                item.setPlatformDefaultValue(SensitiveConfigCrypto.mask(platformPlain));
                item.setTenantValue(StringUtils.hasText(tenantPlain) ? SensitiveConfigCrypto.mask(tenantPlain) : null);
                item.setEffectiveValue(SensitiveConfigCrypto.mask(
                        configService.getString(tenantId, meta.key(), platformPlain)));
            } else {
                item.setPlatformDefaultValue(platformPlain);
                item.setTenantValue(tenantPlain);
                item.setEffectiveValue(configService.getString(tenantId, meta.key(), platformPlain));
            }
            item.setOverridden(StringUtils.hasText(tenantPlain));
            items.add(item);
        }
        return items;
    }

    @Override
    public void upsert(String configKey, TenantPolicyConfigUpdateRequest request) {
        String tenantId = requireTenantId();
        String key = requireOverridableKey(configKey);
        ConfigKeyRegistry.validateValue(key, request.getConfigValue());
        String value = request.getConfigValue() == null ? "" : request.getConfigValue().trim();
        assertPasswordInitialPolicy(tenantId, key, value);
        String sealed = sensitiveConfigCrypto.sealIfSensitive(key, value);

        TenantScopeRunner.runIgnoringTenant(() -> {
            ConfigEntity existing = configMapper.selectByTenantAndKey(tenantId, key);
            if (existing == null) {
                ConfigEntity platform = configMapper.selectByTenantAndKey(TenantConstants.PLATFORM_TENANT_ID, key);
                ConfigEntity entity = new ConfigEntity();
                entity.setId(IdGeneratorUtils.nextIdStr());
                entity.setTenantId(tenantId);
                entity.setConfigKey(key);
                entity.setConfigValue(sealed);
                entity.setConfigType("N");
                if (platform != null) {
                    entity.setConfigName(platform.getConfigName());
                    entity.setRemark(platform.getRemark());
                } else {
                    entity.setConfigName(key);
                }
                AuditFieldHelper.fillInsert(entity);
                configMapper.insert(entity);
            } else {
                existing.setConfigValue(sealed);
                AuditFieldHelper.fillUpdate(existing);
                configMapper.updateById(existing);
            }
            return null;
        });
        configService.evict(tenantId, key);
        configCacheService.evict(tenantId, key);
    }

    @Override
    public void reset(String configKey) {
        String tenantId = requireTenantId();
        if (TenantConstants.PLATFORM_TENANT_ID.equals(tenantId)) {
            throw new BusinessException("error.config.tenant_policy_platform_reset_forbidden");
        }
        String key = requireOverridableKey(configKey);
        TenantScopeRunner.runIgnoringTenant(() -> {
            ConfigEntity existing = configMapper.selectByTenantAndKey(tenantId, key);
            if (existing != null && "Y".equalsIgnoreCase(existing.getConfigType())) {
                throw new BusinessException("error.config.builtin_delete");
            }
            // 恢复平台默认 = 去掉租户覆盖行；物理删除避免 soft-delete 唯一索引堆积
            configMapper.purgeByTenantAndKey(tenantId, key);
            return null;
        });
        configService.evict(tenantId, key);
        configCacheService.evict(tenantId, key);
    }

    @Override
    public void refreshCache() {
        String tenantId = requireTenantId();
        configCacheService.refreshTenant(tenantId);
    }

    private String requireTenantId() {
        String tenantId = SecurityContextHelper.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            throw new BusinessException("error.unauthorized");
        }
        return tenantId;
    }

    private String requireOverridableKey(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            throw new BusinessException("error.forbidden");
        }
        String key = configKey.trim();
        if (!ConfigKeyRegistry.isTenantOverridable(key)) {
            throw new BusinessException("error.config.tenant_policy_key_forbidden", key);
        }
        return key;
    }

    private String loadRaw(String tenantId, String configKey) {
        ConfigEntity entity = TenantScopeRunner.runIgnoringTenant(
                () -> configMapper.selectByTenantAndKey(tenantId, configKey));
        return entity == null ? null : entity.getConfigValue();
    }

    private void assertPasswordInitialPolicy(String tenantId, String configKey, String value) {
        if (!ConfigKeys.PASSWORD_INITIAL.equals(configKey)) {
            return;
        }
        passwordPolicyService.validate(tenantId, value);
    }
}

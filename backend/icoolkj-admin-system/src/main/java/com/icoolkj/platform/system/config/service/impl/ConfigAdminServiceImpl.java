package com.icoolkj.platform.system.config.service.impl;

import com.icoolkj.platform.common.config.ConfigKeyRegistry;
import com.icoolkj.platform.common.constant.ConfigKeys;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.demo.DemoModeProperties;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.security.crypto.SensitiveConfigCrypto;
import com.icoolkj.platform.system.config.convert.ConfigConvert;
import com.icoolkj.platform.system.config.domain.ConfigEntity;
import com.icoolkj.platform.system.config.dto.ConfigCreateRequest;
import com.icoolkj.platform.system.config.dto.ConfigPageQuery;
import com.icoolkj.platform.system.config.dto.ConfigUpdateRequest;
import com.icoolkj.platform.system.config.mapper.ConfigMapper;
import com.icoolkj.platform.system.config.service.ConfigAdminService;
import com.icoolkj.platform.system.config.service.ConfigCacheService;
import com.icoolkj.platform.system.config.service.ConfigService;
import com.icoolkj.platform.system.i18n.service.I18nQueryService;
import com.icoolkj.platform.system.menu.service.MenuAssignScopeService;
import com.icoolkj.platform.system.user.service.PasswordPolicyService;
import com.icoolkj.platform.system.brandpack.support.BrandPackSupport;
import com.icoolkj.platform.system.config.vo.ConfigBootstrapVO;
import com.icoolkj.platform.system.config.vo.ConfigResolvedVO;
import com.icoolkj.platform.system.config.vo.ConfigVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** 参数配置 CRUD 实现 */
@Service
public class ConfigAdminServiceImpl implements ConfigAdminService {

    private final ConfigMapper configMapper;
    private final ConfigCacheService configCacheService;
    private final ConfigService configService;
    private final I18nQueryService i18nQueryService;
    private final MenuAssignScopeService menuAssignScopeService;
    private final DemoModeProperties demoModeProperties;
    private final BrandPackSupport brandPackSupport;
    private final PasswordPolicyService passwordPolicyService;
    private final SensitiveConfigCrypto sensitiveConfigCrypto;

    public ConfigAdminServiceImpl(ConfigMapper configMapper,
                                  ConfigCacheService configCacheService,
                                  ConfigService configService,
                                  I18nQueryService i18nQueryService,
                                  MenuAssignScopeService menuAssignScopeService,
                                  DemoModeProperties demoModeProperties,
                                  BrandPackSupport brandPackSupport,
                                  PasswordPolicyService passwordPolicyService,
                                  SensitiveConfigCrypto sensitiveConfigCrypto) {
        this.configMapper = configMapper;
        this.configCacheService = configCacheService;
        this.configService = configService;
        this.i18nQueryService = i18nQueryService;
        this.menuAssignScopeService = menuAssignScopeService;
        this.demoModeProperties = demoModeProperties;
        this.brandPackSupport = brandPackSupport;
        this.passwordPolicyService = passwordPolicyService;
        this.sensitiveConfigCrypto = sensitiveConfigCrypto;
    }

    @Override
    public PageResult<ConfigVO> page(ConfigPageQuery query) {
        menuAssignScopeService.assertPlatformMenuOperator();
        prepareCategoryFilters(query);
        long total = configMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<ConfigEntity> list = configMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        List<ConfigVO> rows = list.stream().map(this::toMaskedVo).toList();
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    private void prepareCategoryFilters(ConfigPageQuery query) {
        if (query == null || !StringUtils.hasText(query.getConfigKeyExcludePrefixes())) {
            return;
        }
        List<String> prefixes = Arrays.stream(query.getConfigKeyExcludePrefixes().split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        query.setConfigKeyExcludePrefixList(prefixes);
    }

    @Override
    public ConfigVO getById(String id) {
        menuAssignScopeService.assertPlatformMenuOperator();
        ConfigEntity entity = configMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.config");
        }
        return toMaskedVo(entity);
    }

    @Override
    public ConfigVO getByKey(String configKey) {
        ConfigEntity entity = configMapper.selectByTenantAndKey(TenantConstants.PLATFORM_TENANT_ID, configKey);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.config");
        }
        return toMaskedVo(entity);
    }

    @Override
    public ConfigResolvedVO getResolved(String configKey) {
        if (!StringUtils.hasText(configKey) || !ConfigKeyRegistry.isPublicBootstrapKey(configKey)) {
            throw new BusinessException("error.forbidden");
        }
        String tenantId = SecurityContextHelper.getTenantId();
        String value = configService.getString(tenantId, configKey, "");
        ConfigResolvedVO vo = new ConfigResolvedVO();
        vo.setConfigKey(configKey);
        vo.setConfigValue(value != null ? value : "");
        return vo;
    }

    @Override
    public ConfigBootstrapVO getBootstrap() {
        var i18nBootstrap = i18nQueryService.getBootstrap();
        ConfigBootstrapVO vo = new ConfigBootstrapVO();
        vo.setDefaultLocale(i18nBootstrap.getDefaultLocale());
        vo.setSupportedLocales(i18nBootstrap.getSupportedLocales());
        vo.setLocales(i18nBootstrap.getLocales());
        vo.setDemoMode(demoModeProperties.isEnabled());
        vo.setBrand(brandPackSupport.resolve());
        return vo;
    }

    @Override
    public String create(ConfigCreateRequest request) {
        menuAssignScopeService.assertPlatformMenuOperator();
        ConfigKeyRegistry.validateValue(request.getConfigKey(), request.getConfigValue());
        assertPasswordInitialPolicy(TenantConstants.PLATFORM_TENANT_ID, request.getConfigKey(), request.getConfigValue());
        assertConfigKeyAvailable(TenantConstants.PLATFORM_TENANT_ID, request.getConfigKey(), null);
        ConfigEntity entity = ConfigConvert.toEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        entity.setConfigValue(sensitiveConfigCrypto.sealIfSensitive(entity.getConfigKey(), entity.getConfigValue()));
        configMapper.insert(entity);
        evictIfPresent(entity);
        return entity.getId();
    }

    @Override
    public void update(ConfigUpdateRequest request) {
        menuAssignScopeService.assertPlatformMenuOperator();
        ConfigEntity entity = configMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.config");
        }
        String oldKey = entity.getConfigKey();
        String oldTenantId = entity.getTenantId();
        ConfigConvert.copy(request, entity);
        ConfigKeyRegistry.validateValue(entity.getConfigKey(), entity.getConfigValue());
        assertPasswordInitialPolicy(entity.getTenantId(), entity.getConfigKey(), entity.getConfigValue());
        assertConfigKeyAvailable(entity.getTenantId(), entity.getConfigKey(), entity.getId());
        entity.setConfigValue(sensitiveConfigCrypto.sealIfSensitive(entity.getConfigKey(), entity.getConfigValue()));
        configMapper.updateById(entity);
        evict(oldTenantId, oldKey);
        evictIfPresent(entity);
    }

    @Override
    public void deleteById(String id) {
        menuAssignScopeService.assertPlatformMenuOperator();
        ConfigEntity entity = configMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.config");
        }
        if ("Y".equalsIgnoreCase(entity.getConfigType())) {
            throw new BusinessException("error.config.builtin_delete");
        }
        String tenantId = entity.getTenantId();
        String configKey = entity.getConfigKey();
        // 非内置参数物理删除，避免 (tenant,key,deleted) 唯一索引下 soft-delete 堆积
        configMapper.deleteById(id);
        evict(tenantId, configKey);
    }

    @Override
    public void refreshCache() {
        menuAssignScopeService.assertPlatformMenuOperator();
        configCacheService.refreshAll();
    }

    private void evictIfPresent(ConfigEntity entity) {
        if (entity != null && StringUtils.hasText(entity.getTenantId()) && StringUtils.hasText(entity.getConfigKey())) {
            evict(entity.getTenantId(), entity.getConfigKey());
        }
    }

    private void evict(String tenantId, String configKey) {
        configCacheService.evict(tenantId, configKey);
        configService.evict(tenantId, configKey);
    }

    private ConfigVO toMaskedVo(ConfigEntity entity) {
        ConfigVO vo = ConfigConvert.toVo(entity);
        if (vo != null && ConfigKeyRegistry.isSensitive(vo.getConfigKey())) {
            String plain = sensitiveConfigCrypto.open(vo.getConfigKey(), vo.getConfigValue());
            vo.setConfigValue(SensitiveConfigCrypto.mask(plain));
        }
        return vo;
    }

    private void assertPasswordInitialPolicy(String tenantId, String configKey, String configValue) {
        if (!ConfigKeys.PASSWORD_INITIAL.equals(configKey)) {
            return;
        }
        String scopeTenantId = StringUtils.hasText(tenantId) ? tenantId : TenantConstants.PLATFORM_TENANT_ID;
        passwordPolicyService.validate(scopeTenantId, configValue == null ? "" : configValue.trim());
    }

    private void assertConfigKeyAvailable(String tenantId, String configKey, String excludeId) {
        if (!StringUtils.hasText(configKey)) {
            return;
        }
        ConfigEntity existing = configMapper.selectByTenantAndKey(tenantId, configKey.trim());
        if (existing != null && (excludeId == null || !excludeId.equals(existing.getId()))) {
            throw new BusinessException("error.config.key_exists");
        }
    }
}

package com.icoolkj.platform.system.brandpack.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.system.brandpack.domain.BrandPackEntity;
import com.icoolkj.platform.system.brandpack.mapper.BrandPackMapper;
import com.icoolkj.platform.system.brandpack.vo.BrandPackVO;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 解析 {@code sys_brand_pack}：租户行优先，否则回退平台 {@code tenant_id=0}。 */
@Component
public class BrandPackSupport {

    private final BrandPackMapper brandPackMapper;
    private final ObjectMapper objectMapper;
    private final BrandPackChromeResolver chromeResolver;

    public BrandPackSupport(BrandPackMapper brandPackMapper,
                            ObjectMapper objectMapper,
                            BrandPackChromeResolver chromeResolver) {
        this.brandPackMapper = brandPackMapper;
        this.objectMapper = objectMapper;
        this.chromeResolver = chromeResolver;
    }

    public BrandPackVO resolve() {
        String tenantId = SecurityContextHelper.getTenantId();
        return resolveForTenant(tenantId);
    }

    /** 按租户解析品牌包；tenantId 空则平台租户。 */
    public BrandPackVO resolveForTenant(String tenantId) {
        String resolvedTenantId = StringUtils.hasText(tenantId)
                ? tenantId
                : TenantConstants.PLATFORM_TENANT_ID;
        BrandPackEntity entity = selectByTenantId(resolvedTenantId);
        if (entity == null && !TenantConstants.PLATFORM_TENANT_ID.equals(resolvedTenantId)) {
            entity = selectByTenantId(TenantConstants.PLATFORM_TENANT_ID);
        }
        return entity == null ? null : toVo(entity);
    }

    /** 本租户是否存在覆盖行（不含平台回退）。 */
    public boolean hasTenantOverride(String tenantId) {
        if (!StringUtils.hasText(tenantId) || TenantConstants.PLATFORM_TENANT_ID.equals(tenantId)) {
            return false;
        }
        return selectByTenantId(tenantId) != null;
    }

    public BrandPackEntity selectByTenantId(String tenantId) {
        return TenantScopeRunner.runIgnoringTenant(() -> brandPackMapper.selectByTenantId(tenantId));
    }

    public BrandPackVO toVo(BrandPackEntity entity) {
        BrandPackVO vo = new BrandPackVO();
        vo.setProductName(entity.getProductName());
        vo.setProductNameI18n(parseStringMap(entity.getProductNameI18n()));
        vo.setCopyrightBrand(entity.getCopyrightBrand());
        vo.setIcpNo(entity.getIcpNo());
        vo.setIcpUrl(entity.getIcpUrl());
        vo.setDomain(entity.getDomain());
        vo.setSiteUrl(entity.getSiteUrl());
        vo.setEmail(entity.getEmail());
        vo.setContactName(entity.getContactName());
        vo.setContactPhone(entity.getContactPhone());
        Map<String, String> assets = parseStringMap(entity.getAssetsJson());
        Map<String, String> labels = parseStringMap(entity.getLabelsJson());
        vo.setAssets(assets);
        vo.setLabels(labels);
        vo.setChrome(chromeResolver.resolveChromeFileIds(assets));
        vo.setChromeLabels(chromeResolver.resolveChromeLabels(labels));
        return vo;
    }

    public String writeI18n(Map<String, String> i18n) {
        return writeStringMap(i18n);
    }

    public String writeStringMap(Map<String, String> map) {
        Map<String, String> cleaned = cleanStringMap(map);
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(cleaned);
        } catch (Exception ex) {
            return null;
        }
    }

    public Map<String, String> cleanStringMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> cleaned = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry == null || !StringUtils.hasText(entry.getKey())) {
                continue;
            }
            String key = entry.getKey().trim();
            String value = entry.getValue() == null ? "" : entry.getValue().trim();
            if (!StringUtils.hasText(value)) {
                continue;
            }
            cleaned.put(key, value);
        }
        return cleaned;
    }

    private Map<String, String> parseStringMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            Map<String, String> map = objectMapper.readValue(json, new TypeReference<>() {});
            return map == null ? Collections.emptyMap() : cleanStringMap(map);
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }
}

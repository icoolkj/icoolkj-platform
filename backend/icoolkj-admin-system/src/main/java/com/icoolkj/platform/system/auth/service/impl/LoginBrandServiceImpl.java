package com.icoolkj.platform.system.auth.service.impl;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.UnauthorizedException;
import com.icoolkj.platform.framework.tenant.TenantProperties;
import com.icoolkj.platform.system.tenant.domain.TenantEntity;
import com.icoolkj.platform.system.tenant.mapper.TenantMapper;
import com.icoolkj.platform.system.auth.service.LoginBrandService;
import com.icoolkj.platform.system.tenant.service.TenantQueryService;
import com.icoolkj.platform.system.brandpack.support.BrandPackChromeResolver;
import com.icoolkj.platform.system.brandpack.support.BrandPackSupport;
import com.icoolkj.platform.system.auth.vo.AuthLoginBrandVO;
import com.icoolkj.platform.system.brandpack.vo.BrandPackVO;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

@Service
public class LoginBrandServiceImpl implements LoginBrandService {

    private final TenantQueryService tenantQueryService;
    private final TenantMapper tenantMapper;
    private final TenantProperties tenantProperties;
    private final BrandPackSupport brandPackSupport;
    private final BrandPackChromeResolver chromeResolver;

    public LoginBrandServiceImpl(TenantQueryService tenantQueryService,
                                 TenantMapper tenantMapper,
                                 TenantProperties tenantProperties,
                                 BrandPackSupport brandPackSupport,
                                 BrandPackChromeResolver chromeResolver) {
        this.tenantQueryService = tenantQueryService;
        this.tenantMapper = tenantMapper;
        this.tenantProperties = tenantProperties;
        this.brandPackSupport = brandPackSupport;
        this.chromeResolver = chromeResolver;
    }

    @Override
    public AuthLoginBrandVO resolveByTenantCode(String tenantCode) {
        ResolvedTenant resolved = resolveTenant(tenantCode);
        BrandPackVO pack = brandPackSupport.resolveForTenant(resolved.tenantId());
        if (pack == null && !TenantConstants.PLATFORM_TENANT_ID.equals(resolved.tenantId())) {
            pack = brandPackSupport.resolveForTenant(TenantConstants.PLATFORM_TENANT_ID);
        }

        Map<String, List<String>> catalogs = chromeResolver.listEnabledProfileCodesByCatalog();
        AuthLoginBrandVO vo = new AuthLoginBrandVO();
        vo.setTenantMatched(resolved.matched());
        vo.setTenantCode(resolved.displayCode());
        vo.setTenantName(resolved.tenantName());
        vo.setCatalogs(catalogs);
        if (pack == null) {
            return vo;
        }
        vo.setProductName(pack.getProductName());
        vo.setProductNameI18n(pack.getProductNameI18n());

        Map<String, String> packAssets = pack.getAssets();
        Map<String, String> packLabels = pack.getLabels();
        boolean withTenant = resolved.matched() && StringUtils.hasText(resolved.displayCode());
        String encodedTenant = withTenant
                ? UriUtils.encodeQueryParam(resolved.displayCode(), StandardCharsets.UTF_8)
                : null;

        Map<String, Map<String, String>> assetsOut = new LinkedHashMap<>();
        Map<String, Map<String, String>> labelsOut = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> catalogEntry : catalogs.entrySet()) {
            String catalogCode = catalogEntry.getKey();
            List<String> slots = catalogEntry.getValue();
            if (!StringUtils.hasText(catalogCode) || slots == null || slots.isEmpty()) {
                continue;
            }
            Map<String, String> catalogAssets = new LinkedHashMap<>();
            Map<String, String> catalogLabels = new LinkedHashMap<>();
            for (String profileCode : slots) {
                if (!StringUtils.hasText(profileCode)) {
                    continue;
                }
                String code = profileCode.trim();
                String fileId = packAssets.get(code);
                if (!StringUtils.hasText(fileId)) {
                    continue;
                }
                StringBuilder url = new StringBuilder("/auth/login-brand/asset?catalogCode=")
                        .append(UriUtils.encodeQueryParam(catalogCode, StandardCharsets.UTF_8))
                        .append("&profileCode=")
                        .append(UriUtils.encodeQueryParam(code, StandardCharsets.UTF_8));
                if (withTenant) {
                    url.append("&tenantCode=").append(encodedTenant);
                }
                catalogAssets.put(code, url.toString());
                String label = packLabels.get(code);
                if (StringUtils.hasText(label)) {
                    catalogLabels.put(code, label.trim());
                }
            }
            if (!catalogAssets.isEmpty()) {
                assetsOut.put(catalogCode, catalogAssets);
            }
            if (!catalogLabels.isEmpty()) {
                labelsOut.put(catalogCode, catalogLabels);
            }
        }
        vo.setAssets(assetsOut);
        vo.setLabels(labelsOut);
        return vo;
    }

    @Override
    public String resolveAssetFileId(String tenantCode, String catalogCode, String profileCode) {
        if (!StringUtils.hasText(catalogCode) || !StringUtils.hasText(profileCode)) {
            return null;
        }
        String catalog = catalogCode.trim();
        String profile = profileCode.trim();
        List<String> slots = chromeResolver.listEnabledProfileCodesByCatalog().get(catalog);
        if (slots == null || !slots.contains(profile)) {
            return null;
        }

        BrandPackVO pack;
        if (!StringUtils.hasText(tenantCode)) {
            pack = brandPackSupport.resolveForTenant(TenantConstants.PLATFORM_TENANT_ID);
        } else {
            ResolvedTenant resolved = resolveTenant(tenantCode);
            // 未匹配租户时与 login-brand 一致：回退平台包，避免二维码 URL 404 一闪消失
            pack = brandPackSupport.resolveForTenant(
                    resolved.matched() ? resolved.tenantId() : TenantConstants.PLATFORM_TENANT_ID);
            if (pack == null && !TenantConstants.PLATFORM_TENANT_ID.equals(resolved.tenantId())) {
                pack = brandPackSupport.resolveForTenant(TenantConstants.PLATFORM_TENANT_ID);
            }
        }
        if (pack == null) {
            return null;
        }
        String fileId = pack.getAssets().get(profile);
        return StringUtils.hasText(fileId) ? fileId.trim() : null;
    }

    private ResolvedTenant resolveTenant(String tenantCode) {
        String code = tenantCode == null ? "" : tenantCode.trim();
        if (!StringUtils.hasText(code)) {
            String platformCode = tenantProperties.getPlatformTenantCode();
            return new ResolvedTenant(
                    TenantConstants.PLATFORM_TENANT_ID,
                    StringUtils.hasText(platformCode) ? platformCode : "platform",
                    null,
                    false);
        }
        try {
            String tenantId = tenantQueryService.resolveLoginTenantId(code);
            String tenantName = null;
            if (!TenantConstants.PLATFORM_TENANT_ID.equals(tenantId)) {
                TenantEntity tenant = tenantMapper.selectById(tenantId);
                if (tenant != null) {
                    tenantName = tenant.getTenantName();
                }
            }
            return new ResolvedTenant(tenantId, code, tenantName, true);
        } catch (UnauthorizedException ex) {
            return new ResolvedTenant(
                    TenantConstants.PLATFORM_TENANT_ID,
                    code,
                    null,
                    false);
        }
    }

    private record ResolvedTenant(
            String tenantId,
            String displayCode,
            String tenantName,
            boolean matched) {
    }
}

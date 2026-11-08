package com.icoolkj.platform.infra.support;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.infra.domain.InfraFileCatalogItemEntity;
import com.icoolkj.platform.infra.mapper.InfraFileCatalogItemMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 按文件目录槽位校验必传 / 最小数量（业务保存前调用）。
 */
@Component
public class CatalogRequiredAssetsValidator {

    private final InfraFileCatalogItemMapper catalogItemMapper;
    private final BizCatalogAssetsSupport assetsSupport;

    public CatalogRequiredAssetsValidator(
            InfraFileCatalogItemMapper catalogItemMapper, BizCatalogAssetsSupport assetsSupport) {
        this.catalogItemMapper = catalogItemMapper;
        this.assetsSupport = assetsSupport;
    }

    public void validateRequired(Map<String, Object> assets, String catalogCode) {
        if (!StringUtils.hasText(catalogCode)) {
            return;
        }
        Map<String, Object> cleaned = assetsSupport.cleanAssets(assets);
        List<String> missing = new ArrayList<>();
        for (InfraFileCatalogItemEntity item : loadCatalogItems(catalogCode.trim())) {
            if (item == null || !StringUtils.hasText(item.getProfileCode())) {
                continue;
            }
            if (!CommonConstants.STATUS_NORMAL.equals(item.getStatus())) {
                continue;
            }
            boolean required = CommonConstants.YES.equals(item.getRequired());
            int min = item.getMinNum() != null ? item.getMinNum() : 0;
            int need = required ? Math.max(min, 1) : min;
            if (need <= 0) {
                continue;
            }
            if (BizCatalogAssetsSupport.countSlotFiles(cleaned.get(item.getProfileCode().trim()))
                    < need) {
                missing.add(item.getProfileCode().trim());
            }
        }
        if (!missing.isEmpty()) {
            throw new BusinessException("error.file.attachment_required", String.join(", ", missing));
        }
    }

    private List<InfraFileCatalogItemEntity> loadCatalogItems(String catalogCode) {
        String tid = SecurityContextHelper.getTenantId();
        return TenantScopeRunner.runIgnoringTenant(() -> {
            List<InfraFileCatalogItemEntity> rows =
                    catalogItemMapper.selectByTenantAndCatalog(tid, catalogCode);
            if (CollectionUtils.isEmpty(rows)
                    && !TenantConstants.PLATFORM_TENANT_ID.equals(tid)) {
                return catalogItemMapper.selectByTenantAndCatalog(
                        TenantConstants.PLATFORM_TENANT_ID, catalogCode);
            }
            return rows != null ? rows : Collections.emptyList();
        });
    }
}

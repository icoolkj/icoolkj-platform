package com.icoolkj.platform.system.brandpack.service.impl;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantProperties;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.infra.dto.InfraFileRefBinding;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.system.brandpack.constant.BrandPackFileConstants;
import com.icoolkj.platform.system.brandpack.domain.BrandPackEntity;
import com.icoolkj.platform.system.tenant.domain.TenantEntity;
import com.icoolkj.platform.system.brandpack.dto.BrandPackUpdateRequest;
import com.icoolkj.platform.system.brandpack.mapper.BrandPackMapper;
import com.icoolkj.platform.system.tenant.mapper.TenantMapper;
import com.icoolkj.platform.system.brandpack.service.BrandPackAdminService;
import com.icoolkj.platform.system.brandpack.support.BrandPackSupport;
import com.icoolkj.platform.system.brandpack.vo.BrandPackAdminVO;
import com.icoolkj.platform.system.brandpack.vo.BrandPackVO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BrandPackAdminServiceImpl implements BrandPackAdminService {

    private static final String REMARK =
            "OEM/登录版权/产品名与多规格 Logo；平台默认，租户可整包覆盖；bootstrap 公开";

    private final BrandPackSupport brandPackSupport;
    private final BrandPackMapper brandPackMapper;
    private final TenantMapper tenantMapper;
    private final TenantProperties tenantProperties;
    private final InfraFileService infraFileService;

    public BrandPackAdminServiceImpl(BrandPackSupport brandPackSupport,
                                     BrandPackMapper brandPackMapper,
                                     TenantMapper tenantMapper,
                                     TenantProperties tenantProperties,
                                     InfraFileService infraFileService) {
        this.brandPackSupport = brandPackSupport;
        this.brandPackMapper = brandPackMapper;
        this.tenantMapper = tenantMapper;
        this.tenantProperties = tenantProperties;
        this.infraFileService = infraFileService;
    }

    @Override
    public BrandPackAdminVO getForAdmin(String targetTenantId) {
        String tid = resolveTargetTenantId(targetTenantId);
        BrandPackVO pack = brandPackSupport.resolveForTenant(tid);
        BrandPackAdminVO vo = new BrandPackAdminVO();
        if (pack != null) {
            BeanUtils.copyProperties(pack, vo);
            vo.setAssets(pack.getAssets());
            vo.setLabels(pack.getLabels());
            vo.setChrome(pack.getChrome());
            vo.setChromeLabels(pack.getChromeLabels());
        }
        fillScopeMeta(vo, tid);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String targetTenantId, BrandPackUpdateRequest request) {
        String tid = resolveTargetTenantId(targetTenantId);
        BrandPackVO pack = toPack(request);
        upsertBrandPack(tid, pack);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetOverride(String targetTenantId) {
        String tid = resolveTargetTenantId(targetTenantId);
        if (TenantConstants.PLATFORM_TENANT_ID.equals(tid)) {
            throw new BusinessException("error.brand.platform_reset_forbidden");
        }
        TenantScopeRunner.runIgnoringTenant(() -> {
            BrandPackEntity existing = brandPackMapper.selectByTenantId(tid);
            if (existing != null) {
                releaseBrandFileRefs(existing);
                brandPackMapper.deleteById(existing.getId());
            }
        });
    }

    private void upsertBrandPack(String tenantId, BrandPackVO pack) {
        TenantScopeRunner.runIgnoringTenant(() -> {
            BrandPackEntity existing = brandPackMapper.selectByTenantId(tenantId);
            if (existing == null) {
                BrandPackEntity entity = new BrandPackEntity();
                entity.setId(IdGeneratorUtils.nextIdStr());
                entity.setTenantId(tenantId);
                applyPack(entity, pack);
                entity.setRemark(REMARK);
                AuditFieldHelper.fillInsert(entity);
                brandPackMapper.insert(entity);
                syncBrandFileRefs(entity, pack);
            } else {
                applyPack(existing, pack);
                existing.setRemark(REMARK);
                AuditFieldHelper.fillUpdate(existing);
                brandPackMapper.updateById(existing);
                syncBrandFileRefs(existing, pack);
            }
        });
    }

    private void applyPack(BrandPackEntity entity, BrandPackVO pack) {
        entity.setProductName(nullToEmpty(pack.getProductName()));
        entity.setProductNameI18n(brandPackSupport.writeI18n(pack.getProductNameI18n()));
        entity.setCopyrightBrand(trimToNull(pack.getCopyrightBrand()));
        entity.setIcpNo(trimToNull(pack.getIcpNo()));
        entity.setIcpUrl(trimToNull(pack.getIcpUrl()));
        entity.setDomain(trimToNull(pack.getDomain()));
        entity.setSiteUrl(trimToNull(pack.getSiteUrl()));
        entity.setEmail(trimToNull(pack.getEmail()));
        entity.setContactName(trimToNull(pack.getContactName()));
        entity.setContactPhone(trimToNull(pack.getContactPhone()));
        entity.setAssetsJson(brandPackSupport.writeStringMap(pack.getAssets()));
        entity.setLabelsJson(brandPackSupport.writeStringMap(pack.getLabels()));
    }

    private void syncBrandFileRefs(BrandPackEntity entity, BrandPackVO pack) {
        if (!StringUtils.hasText(entity.getTenantId()) || !StringUtils.hasText(entity.getId())) {
            return;
        }
        Map<String, String> assets = pack.getAssets();
        List<InfraFileRefBinding> bindings = new ArrayList<>();
        if (assets != null) {
            for (Map.Entry<String, String> entry : assets.entrySet()) {
                if (entry == null || !StringUtils.hasText(entry.getKey()) || !StringUtils.hasText(entry.getValue())) {
                    continue;
                }
                bindings.add(new InfraFileRefBinding(entry.getValue().trim(), entry.getKey().trim()));
            }
        }
        infraFileService.syncBizFileRefs(
                entity.getTenantId(), BrandPackFileConstants.BIZ_TYPE, entity.getId(), bindings);
    }

    private void releaseBrandFileRefs(BrandPackEntity entity) {
        if (!StringUtils.hasText(entity.getTenantId()) || !StringUtils.hasText(entity.getId())) {
            return;
        }
        infraFileService.releaseBizFiles(
                entity.getTenantId(), BrandPackFileConstants.BIZ_TYPE, entity.getId());
    }

    private BrandPackVO toPack(BrandPackUpdateRequest request) {
        BrandPackVO pack = new BrandPackVO();
        pack.setProductName(trim(request.getProductName()));
        pack.setProductNameI18n(request.getProductNameI18n());
        pack.setCopyrightBrand(trim(request.getCopyrightBrand()));
        pack.setIcpNo(trim(request.getIcpNo()));
        pack.setIcpUrl(trim(request.getIcpUrl()));
        pack.setDomain(trim(request.getDomain()));
        pack.setSiteUrl(trim(request.getSiteUrl()));
        pack.setEmail(trim(request.getEmail()));
        pack.setContactName(trim(request.getContactName()));
        pack.setContactPhone(trim(request.getContactPhone()));
        pack.setAssets(brandPackSupport.cleanStringMap(request.getAssets()));
        pack.setLabels(brandPackSupport.cleanStringMap(request.getLabels()));
        return pack;
    }

    private void fillScopeMeta(BrandPackAdminVO vo, String tid) {
        vo.setTargetTenantId(tid);
        vo.setCanSelectTenant(isPlatformOperator());
        vo.setTenantOverride(false);
        if (TenantConstants.PLATFORM_TENANT_ID.equals(tid)) {
            String code = tenantProperties.getPlatformTenantCode();
            vo.setTargetTenantCode(StringUtils.hasText(code) ? code : "platform");
            vo.setTargetTenantName(null);
            return;
        }
        TenantEntity tenant = tenantMapper.selectById(tid);
        if (tenant != null) {
            vo.setTargetTenantCode(tenant.getTenantCode());
            vo.setTargetTenantName(tenant.getTenantName());
        }
        vo.setTenantOverride(brandPackSupport.hasTenantOverride(tid));
    }

    private String resolveTargetTenantId(String requested) {
        String current = SecurityContextHelper.getTenantId();
        if (!StringUtils.hasText(current)) {
            throw new BusinessException("error.unauthorized");
        }
        if (!isPlatformOperator()) {
            if (StringUtils.hasText(requested) && !current.equals(requested.trim())) {
                throw new BusinessException("error.brand.tenant_scope_forbidden");
            }
            return current;
        }
        if (!StringUtils.hasText(requested)) {
            return TenantConstants.PLATFORM_TENANT_ID;
        }
        String tid = requested.trim();
        if (TenantConstants.PLATFORM_TENANT_ID.equals(tid)) {
            return tid;
        }
        TenantEntity tenant = tenantMapper.selectById(tid);
        if (tenant == null || (tenant.getDeleted() != null && tenant.getDeleted() != 0)) {
            throw new BusinessException("error.entity.not_found", "entity.name.tenant");
        }
        return tid;
    }

    private boolean isPlatformOperator() {
        String current = SecurityContextHelper.getTenantId();
        return TenantConstants.PLATFORM_TENANT_ID.equals(current);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}

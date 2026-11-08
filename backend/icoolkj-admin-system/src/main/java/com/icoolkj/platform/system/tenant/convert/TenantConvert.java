package com.icoolkj.platform.system.tenant.convert;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.system.tenant.domain.TenantEntity;
import com.icoolkj.platform.system.tenant.dto.TenantCreateRequest;
import com.icoolkj.platform.system.tenant.dto.TenantUpdateRequest;
import com.icoolkj.platform.system.tenant.vo.TenantVO;
import org.springframework.util.StringUtils;

/** 租户表对象转换 */
public final class TenantConvert {

    private TenantConvert() {
    }

    public static TenantVO toVo(TenantEntity entity) {
        if (entity == null) {
            return null;
        }
        TenantVO vo = new TenantVO();
        vo.setId(entity.getId());
        vo.setTenantCode(entity.getTenantCode());
        vo.setTenantName(entity.getTenantName());
        vo.setContactName(entity.getContactName());
        vo.setContactPhone(entity.getContactPhone());
        vo.setContactEmail(entity.getContactEmail());
        vo.setLicenseNo(entity.getLicenseNo());
        vo.setExpireAt(entity.getExpireAt());
        vo.setAccountLimit(entity.getAccountLimit());
        vo.setStatus(entity.getStatus());
        vo.setTenantType(entity.getTenantType());
        vo.setParentTenantId(entity.getParentTenantId());
        vo.setRootRegionId(entity.getRootRegionId());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    public static TenantEntity toEntity(TenantCreateRequest request) {
        TenantEntity entity = new TenantEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(null));
        entity.setTenantCode(request.getTenantCode());
        entity.setTenantName(request.getTenantName());
        entity.setContactName(request.getContactName());
        entity.setContactPhone(request.getContactPhone());
        entity.setContactEmail(request.getContactEmail());
        entity.setLicenseNo(request.getLicenseNo());
        entity.setExpireAt(request.getExpireAt());
        entity.setAccountLimit(request.getAccountLimit());
        entity.setStatus(request.getStatus());
        entity.setTenantType(request.getTenantType());
        entity.setParentTenantId(normalizeParentTenantId(request.getParentTenantId()));
        entity.setRootRegionId(request.getRootRegionId());
        entity.setRemark(request.getRemark());
        return entity;
    }

    public static void copy(TenantUpdateRequest request, TenantEntity entity) {
        entity.setTenantCode(request.getTenantCode());
        entity.setTenantName(request.getTenantName());
        entity.setContactName(request.getContactName());
        entity.setContactPhone(request.getContactPhone());
        entity.setContactEmail(request.getContactEmail());
        entity.setLicenseNo(request.getLicenseNo());
        entity.setExpireAt(request.getExpireAt());
        entity.setAccountLimit(request.getAccountLimit());
        entity.setStatus(request.getStatus());
        if (request.getParentTenantId() != null) {
            entity.setParentTenantId(normalizeParentTenantId(request.getParentTenantId()));
        }
        if (StringUtils.hasText(request.getRootRegionId())) {
            entity.setRootRegionId(request.getRootRegionId());
        }
        entity.setRemark(request.getRemark());
    }

    private static String normalizeParentTenantId(String parentTenantId) {
        if (!org.springframework.util.StringUtils.hasText(parentTenantId)
                || TenantConstants.PLATFORM_TENANT_ID.equals(parentTenantId.trim())) {
            return null;
        }
        return parentTenantId.trim();
    }
}

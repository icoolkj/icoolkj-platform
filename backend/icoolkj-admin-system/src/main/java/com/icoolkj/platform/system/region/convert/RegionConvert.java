package com.icoolkj.platform.system.region.convert;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.system.region.domain.RegionEntity;
import com.icoolkj.platform.system.region.dto.RegionCreateRequest;
import com.icoolkj.platform.system.region.dto.RegionUpdateRequest;
import com.icoolkj.platform.system.region.vo.RegionVO;

public final class RegionConvert {

    private RegionConvert() {
    }

    public static RegionVO toVo(RegionEntity entity) {
        if (entity == null) {
            return null;
        }
        RegionVO vo = new RegionVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setAncestors(entity.getAncestors());
        vo.setRegionCode(entity.getRegionCode());
        vo.setRegionName(entity.getRegionName());
        vo.setRegionLevel(entity.getRegionLevel());
        vo.setSortNo(entity.getSortNo());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    public static RegionEntity toEntity(RegionCreateRequest request) {
        RegionEntity entity = new RegionEntity();
        entity.setTenantId(TenantConstants.PLATFORM_TENANT_ID);
        entity.setRegionCode(request.getRegionCode());
        entity.setRegionName(request.getRegionName());
        entity.setRegionLevel(request.getRegionLevel());
        entity.setSortNo(request.getSortNo());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
        return entity;
    }

    public static void copy(RegionUpdateRequest request, RegionEntity entity) {
        entity.setRegionCode(request.getRegionCode());
        entity.setRegionName(request.getRegionName());
        entity.setRegionLevel(request.getRegionLevel());
        entity.setSortNo(request.getSortNo());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
    }
}

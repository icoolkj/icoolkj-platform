package com.icoolkj.platform.system.banner.convert;

import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.system.banner.domain.BannerEntity;
import com.icoolkj.platform.system.banner.dto.BannerCreateRequest;
import com.icoolkj.platform.system.banner.dto.BannerUpdateRequest;
import com.icoolkj.platform.system.banner.vo.BannerListVO;
import com.icoolkj.platform.system.banner.vo.BannerPublishedVO;
import com.icoolkj.platform.system.banner.vo.BannerVO;
import org.springframework.util.StringUtils;

/** 轮播图对象转换 */
public final class BannerConvert {

    private BannerConvert() {
    }

    public static BannerVO toVo(BannerEntity entity) {
        if (entity == null) {
            return null;
        }
        BannerVO vo = new BannerVO();
        vo.setId(entity.getId());
        vo.setBannerTitle(entity.getBannerTitle());
        vo.setImageFileId(entity.getImageFileId());
        vo.setImagePreviewUrl(entity.getImagePreviewUrl());
        vo.setDisplayPositions(entity.getDisplayPositions());
        vo.setPositionConfig(entity.getPositionConfig());
        vo.setSortNo(entity.getSortNo());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static BannerListVO toListVo(BannerEntity entity) {
        if (entity == null) {
            return null;
        }
        BannerListVO vo = new BannerListVO();
        vo.setId(entity.getId());
        vo.setBannerTitle(entity.getBannerTitle());
        vo.setImageFileId(entity.getImageFileId());
        vo.setImagePreviewUrl(entity.getImagePreviewUrl());
        vo.setDisplayPositions(entity.getDisplayPositions());
        vo.setSortNo(entity.getSortNo());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static BannerPublishedVO toPublishedVo(BannerEntity entity) {
        if (entity == null) {
            return null;
        }
        BannerPublishedVO vo = new BannerPublishedVO();
        vo.setId(entity.getId());
        vo.setBannerTitle(entity.getBannerTitle());
        vo.setImageFileId(entity.getImageFileId());
        vo.setImagePreviewUrl(entity.getImagePreviewUrl());
        vo.setDisplayPositions(entity.getDisplayPositions());
        vo.setPositionConfig(entity.getPositionConfig());
        vo.setSortNo(entity.getSortNo());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static BannerEntity toEntity(BannerCreateRequest request) {
        BannerEntity entity = new BannerEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(null));
        entity.setBannerTitle(request.getBannerTitle());
        entity.setImageFileId(request.getImageFileId().trim());
        entity.setDisplayPositions(request.getDisplayPositions());
        entity.setPositionConfig(trimToNull(request.getPositionConfig()));
        entity.setSortNo(request.getSortNo() != null ? request.getSortNo() : 99);
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
        return entity;
    }

    public static void copy(BannerUpdateRequest request, BannerEntity entity) {
        entity.setBannerTitle(request.getBannerTitle());
        entity.setImageFileId(request.getImageFileId().trim());
        entity.setDisplayPositions(request.getDisplayPositions());
        entity.setPositionConfig(trimToNull(request.getPositionConfig()));
        entity.setSortNo(request.getSortNo());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

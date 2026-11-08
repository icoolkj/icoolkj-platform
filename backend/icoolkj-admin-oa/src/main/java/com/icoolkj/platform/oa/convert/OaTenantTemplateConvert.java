package com.icoolkj.platform.oa.convert;

import com.icoolkj.platform.oa.domain.OaTenantTemplateEntity;
import com.icoolkj.platform.oa.domain.OaTenantTemplateVersionEntity;
import com.icoolkj.platform.oa.dto.OaTenantTemplateUpdateRequest;
import com.icoolkj.platform.oa.vo.OaTenantTemplateDetailVO;
import com.icoolkj.platform.oa.vo.OaTenantTemplateVO;

/** OA 租户模板对象转换（代码生成基线） */
public final class OaTenantTemplateConvert {

    private OaTenantTemplateConvert() {
    }

    public static OaTenantTemplateVO toVo(OaTenantTemplateEntity entity) {
        if (entity == null) {
            return null;
        }
        OaTenantTemplateVO vo = new OaTenantTemplateVO();
        vo.setId(entity.getId());
        vo.setCategoryCode(entity.getCategoryCode());
        vo.setTemplateCode(entity.getTemplateCode());
        vo.setTemplateName(entity.getTemplateName());
        vo.setPlatformTemplateId(entity.getPlatformTemplateId());
        vo.setSourcePlatformTplVerId(entity.getSourcePlatformTplVerId());
        vo.setLatestVersionNo(entity.getLatestVersionNo());
        vo.setPublishedTplVerId(entity.getPublishedTplVerId());
        vo.setEnableStatus(entity.getEnableStatus());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    public static void fillDraftInfo(OaTenantTemplateVO vo, OaTenantTemplateVersionEntity draft) {
        if (vo == null) {
            return;
        }
        vo.setHasDraft(draft != null);
        vo.setDraftVersionNo(draft != null ? draft.getVersionNo() : null);
    }

    public static void fillPlatformSyncInfo(OaTenantTemplateVO vo, Integer platformLatestVersionNo,
                                           boolean hasSyncChanges) {
        if (vo == null) {
            return;
        }
        vo.setPlatformLatestVersionNo(platformLatestVersionNo);
        vo.setPlatformOutdated(hasSyncChanges);
    }

    public static OaTenantTemplateDetailVO toDetailVo(OaTenantTemplateVO base) {
        if (base == null) {
            return null;
        }
        OaTenantTemplateDetailVO detail = new OaTenantTemplateDetailVO();
        detail.setId(base.getId());
        detail.setCategoryCode(base.getCategoryCode());
        detail.setTemplateCode(base.getTemplateCode());
        detail.setTemplateName(base.getTemplateName());
        detail.setPlatformTemplateId(base.getPlatformTemplateId());
        detail.setSourcePlatformTplVerId(base.getSourcePlatformTplVerId());
        detail.setLatestVersionNo(base.getLatestVersionNo());
        detail.setDraftVersionNo(base.getDraftVersionNo());
        detail.setHasDraft(base.getHasDraft());
        detail.setPlatformLatestVersionNo(base.getPlatformLatestVersionNo());
        detail.setPlatformOutdated(base.getPlatformOutdated());
        detail.setPublishedTplVerId(base.getPublishedTplVerId());
        detail.setEnableStatus(base.getEnableStatus());
        detail.setStatus(base.getStatus());
        detail.setRemark(base.getRemark());
        detail.setCreatedAt(base.getCreatedAt());
        detail.setUpdatedAt(base.getUpdatedAt());
        return detail;
    }

    public static void copy(OaTenantTemplateUpdateRequest request, OaTenantTemplateEntity entity) {
        entity.setTemplateName(request.getTemplateName().trim());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        entity.setRemark(request.getRemark());
    }
}

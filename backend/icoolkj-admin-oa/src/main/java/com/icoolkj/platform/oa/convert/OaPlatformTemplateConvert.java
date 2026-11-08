package com.icoolkj.platform.oa.convert;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.oa.domain.OaPlatformTemplateEntity;
import com.icoolkj.platform.oa.domain.OaPlatformTemplateVersionEntity;
import com.icoolkj.platform.oa.dto.OaPlatformTemplateCreateRequest;
import com.icoolkj.platform.oa.dto.OaPlatformTemplateUpdateRequest;
import com.icoolkj.platform.oa.vo.OaPlatformTemplateDetailVO;
import com.icoolkj.platform.oa.vo.OaPlatformTemplateVO;

/** OA 平台模板对象转换（代码生成基线） */
public final class OaPlatformTemplateConvert {

    private OaPlatformTemplateConvert() {
    }

    public static OaPlatformTemplateVO toVo(OaPlatformTemplateEntity entity) {
        if (entity == null) {
            return null;
        }
        OaPlatformTemplateVO vo = new OaPlatformTemplateVO();
        vo.setId(entity.getId());
        vo.setCategoryCode(entity.getCategoryCode());
        vo.setTemplateCode(entity.getTemplateCode());
        vo.setTemplateName(entity.getTemplateName());
        vo.setWfModelId(entity.getWfModelId());
        vo.setLatestVersionNo(entity.getLatestVersionNo());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    public static void fillDraftInfo(OaPlatformTemplateVO vo, OaPlatformTemplateVersionEntity draft) {
        if (vo == null) {
            return;
        }
        vo.setHasDraft(draft != null);
        vo.setDraftVersionNo(draft != null ? draft.getVersionNo() : null);
    }

    public static OaPlatformTemplateDetailVO toDetailVo(OaPlatformTemplateVO base) {
        if (base == null) {
            return null;
        }
        OaPlatformTemplateDetailVO detail = new OaPlatformTemplateDetailVO();
        detail.setId(base.getId());
        detail.setCategoryCode(base.getCategoryCode());
        detail.setTemplateCode(base.getTemplateCode());
        detail.setTemplateName(base.getTemplateName());
        detail.setWfModelId(base.getWfModelId());
        detail.setLatestVersionNo(base.getLatestVersionNo());
        detail.setDraftVersionNo(base.getDraftVersionNo());
        detail.setHasDraft(base.getHasDraft());
        detail.setStatus(base.getStatus());
        detail.setRemark(base.getRemark());
        detail.setCreatedAt(base.getCreatedAt());
        detail.setUpdatedAt(base.getUpdatedAt());
        return detail;
    }

    public static OaPlatformTemplateEntity toEntity(OaPlatformTemplateCreateRequest request, String tenantId) {
        OaPlatformTemplateEntity entity = new OaPlatformTemplateEntity();
        entity.setTenantId(tenantId);
        entity.setCategoryCode(request.getCategoryCode().trim());
        entity.setTemplateCode(request.getTemplateCode().trim());
        entity.setTemplateName(request.getTemplateName().trim());
        entity.setWfModelId(trimToNull(request.getWfModelId()));
        entity.setLatestVersionNo(0);
        entity.setStatus(defaultStatus(request.getStatus()));
        entity.setRemark(trimToNull(request.getRemark()));
        return entity;
    }

    public static void copy(OaPlatformTemplateUpdateRequest request, OaPlatformTemplateEntity entity) {
        entity.setTemplateName(request.getTemplateName().trim());
        entity.setWfModelId(trimToNull(request.getWfModelId()));
        if (request.getStatus() != null) {
            entity.setStatus(defaultStatus(request.getStatus()));
        }
        entity.setRemark(trimToNull(request.getRemark()));
    }

    private static String defaultStatus(String status) {
        return status == null || status.isBlank() ? CommonConstants.STATUS_NORMAL : status.trim();
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

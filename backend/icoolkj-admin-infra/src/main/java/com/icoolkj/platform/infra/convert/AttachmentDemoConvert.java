package com.icoolkj.platform.infra.convert;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.infra.domain.AttachmentDemoEntity;
import com.icoolkj.platform.infra.dto.AttachmentDemoCreateRequest;
import com.icoolkj.platform.infra.dto.AttachmentDemoUpdateRequest;
import com.icoolkj.platform.infra.support.BizCatalogAssetsSupport;
import com.icoolkj.platform.infra.vo.AttachmentDemoVO;
import org.springframework.util.StringUtils;

/** 附件测试业务对象转换 */
public final class AttachmentDemoConvert {

    private AttachmentDemoConvert() {
    }

    public static AttachmentDemoVO toVo(AttachmentDemoEntity entity, BizCatalogAssetsSupport assetsSupport) {
        if (entity == null) {
            return null;
        }
        AttachmentDemoVO vo = new AttachmentDemoVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setAssets(assetsSupport.parseAssets(entity.getAssetsJson()));
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static AttachmentDemoEntity toEntity(
            AttachmentDemoCreateRequest request, BizCatalogAssetsSupport assetsSupport) {
        AttachmentDemoEntity entity = new AttachmentDemoEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(null));
        entity.setTitle(request.getTitle().trim());
        entity.setAssetsJson(assetsSupport.writeAssets(request.getAssets()));
        entity.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : CommonConstants.STATUS_NORMAL);
        entity.setRemark(trimToNull(request.getRemark()));
        return entity;
    }

    public static void copy(
            AttachmentDemoUpdateRequest request,
            AttachmentDemoEntity entity,
            BizCatalogAssetsSupport assetsSupport) {
        entity.setTitle(request.getTitle().trim());
        entity.setAssetsJson(assetsSupport.writeAssets(request.getAssets()));
        entity.setStatus(request.getStatus());
        entity.setRemark(trimToNull(request.getRemark()));
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

package com.icoolkj.platform.oa.support;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.oa.domain.OaPlatformTemplateEntity;
import com.icoolkj.platform.oa.mapper.OaPlatformTemplateMapper;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** OA 平台模板操作边界：仅平台管理员可读写 tenant_id=0 的模板 */
@Component
public class OaPlatformTemplateAccessSupport {

    private final OaPlatformTemplateMapper templateMapper;

    public OaPlatformTemplateAccessSupport(OaPlatformTemplateMapper templateMapper) {
        this.templateMapper = templateMapper;
    }

    public OaPlatformTemplateEntity requireById(String id) {
        OaTenantSupport.assertPlatformOperator();
        OaPlatformTemplateEntity entity = templateMapper.selectById(id);
        if (entity == null || !Objects.equals(OaTenantSupport.platformTenantId(), entity.getTenantId())) {
            throw new BusinessException("error.oa.template_not_found");
        }
        return entity;
    }
}

package com.icoolkj.platform.oa.support;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.oa.domain.OaTenantTemplateEntity;
import com.icoolkj.platform.oa.mapper.OaTenantTemplateMapper;
import org.springframework.stereotype.Component;

/** OA 租户模板操作边界：租户管理员仅可读写本租户映射 */
@Component
public class OaTenantTemplateAccessSupport {

    private final OaTenantTemplateMapper tenantTemplateMapper;

    public OaTenantTemplateAccessSupport(OaTenantTemplateMapper tenantTemplateMapper) {
        this.tenantTemplateMapper = tenantTemplateMapper;
    }

    public OaTenantTemplateEntity requireById(String id) {
        OaTenantSupport.assertTenantOperator();
        OaTenantTemplateEntity entity = tenantTemplateMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.oa.template_not_found");
        }
        TenantQueryScope.assertReadable(entity.getTenantId());
        return entity;
    }
}

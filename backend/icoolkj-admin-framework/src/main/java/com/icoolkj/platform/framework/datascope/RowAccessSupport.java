package com.icoolkj.platform.framework.datascope;

import com.icoolkj.platform.framework.auth.DataPermissionService;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import org.springframework.stereotype.Component;

@Component
public class RowAccessSupport {

    private final DataPermissionService dataPermissionService;

    public RowAccessSupport(DataPermissionService dataPermissionService) {
        this.dataPermissionService = dataPermissionService;
    }

    public void assertTenant(String entityTenantId) {
        TenantQueryScope.assertReadable(entityTenantId);
    }

    public void assertDataScope(DataScopeSpec spec, String ownerUserId, String ownerDeptId) {
        dataPermissionService.assertReadable(spec, ownerUserId, ownerDeptId);
    }

    public void assertReadable(String entityTenantId,
                               DataScopeSpec spec,
                               String ownerUserId,
                               String ownerDeptId) {
        assertTenant(entityTenantId);
        assertDataScope(spec, ownerUserId, ownerDeptId);
    }
}

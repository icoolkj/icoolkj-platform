package com.icoolkj.platform.framework.auth;

import com.icoolkj.platform.framework.annotation.DataScope;
import com.icoolkj.platform.framework.datascope.DataScopeSpec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(DataPermissionService.class)
public class DataPermissionServiceImpl implements DataPermissionService {

    @Override
    public String buildDataScopeSql(DataScope dataScope) {
        return "";
    }

    @Override
    public boolean isReadable(DataScopeSpec spec, String ownerUserId, String ownerDeptId) {
        return true;
    }

    @Override
    public void assertReadable(DataScopeSpec spec, String ownerUserId, String ownerDeptId) {
        // no-op when system module is absent
    }
}

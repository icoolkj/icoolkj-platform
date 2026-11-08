package com.icoolkj.platform.framework.auth;

import com.icoolkj.platform.framework.annotation.DataScope;
import com.icoolkj.platform.framework.datascope.DataScopeSpec;

public interface DataPermissionService {

    String buildDataScopeSql(DataScope dataScope);

    boolean isReadable(DataScopeSpec spec, String ownerUserId, String ownerDeptId);

    void assertReadable(DataScopeSpec spec, String ownerUserId, String ownerDeptId);
}

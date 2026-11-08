package com.icoolkj.platform.system.datascope;

import com.icoolkj.platform.framework.annotation.DataScope;

/** 单条角色数据权限谓词构建上下文。 */
public record DataScopePredicateContext(
        DataScope dataScope,
        String tenantId,
        String userId,
        String deptId,
        String roleId
) {
}

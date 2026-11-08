package com.icoolkj.platform.system.tenant.service;

import com.icoolkj.platform.system.tenant.domain.TenantEntity;
import com.icoolkj.platform.system.tenant.dto.TenantBootstrapOptions;
import com.icoolkj.platform.system.tenant.vo.TenantProvisionVO;

/**
 * 新租户开户：根部门、默认岗位、超级管理员角色与首个管理员用户。
 */
public interface TenantBootstrapService {

    /**
     * 在已插入的租户上开通组织与管理员；须在写租户同一事务内调用。
     */
    TenantProvisionVO bootstrap(TenantEntity tenant, TenantBootstrapOptions options);
}

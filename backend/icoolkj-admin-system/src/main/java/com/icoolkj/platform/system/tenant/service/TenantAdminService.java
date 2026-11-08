package com.icoolkj.platform.system.tenant.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.tenant.dto.TenantAdminPageQuery;
import com.icoolkj.platform.system.tenant.vo.TenantAdminVO;
import com.icoolkj.platform.system.tenant.vo.TenantProvisionVO;

/** 平台侧租户超级管理员运维（重置密码 / 解锁）。 */
public interface TenantAdminService {

    PageResult<TenantAdminVO> page(TenantAdminPageQuery query);

    /** 重置为标准初始密码，返回一次性明文；并吊销会话。 */
    TenantProvisionVO resetPassword(String userId);

    /** 清除登录失败锁定。 */
    void unlock(String userId);
}

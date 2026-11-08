package com.icoolkj.platform.system.tenant.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.tenant.dto.TenantCreateRequest;
import com.icoolkj.platform.system.tenant.dto.TenantPageQuery;
import com.icoolkj.platform.system.tenant.dto.TenantStatusUpdateRequest;
import com.icoolkj.platform.system.tenant.dto.TenantUpdateRequest;
import com.icoolkj.platform.system.tenant.vo.TenantProvisionVO;
import com.icoolkj.platform.system.tenant.vo.TenantVO;

/** 租户表服务 */
public interface TenantService {

    PageResult<TenantVO> page(TenantPageQuery query);

    TenantVO getById(String id);

    /** 创建租户并开户（根部门 / 管理员），返回一次性开通信息。 */
    TenantProvisionVO create(TenantCreateRequest request);

    void update(TenantUpdateRequest request);

    void updateStatus(TenantStatusUpdateRequest request);

    void deleteById(String id);
}

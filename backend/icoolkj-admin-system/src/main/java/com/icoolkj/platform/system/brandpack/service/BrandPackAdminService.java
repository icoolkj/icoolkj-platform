package com.icoolkj.platform.system.brandpack.service;

import com.icoolkj.platform.system.brandpack.dto.BrandPackUpdateRequest;
import com.icoolkj.platform.system.brandpack.vo.BrandPackAdminVO;

public interface BrandPackAdminService {

    BrandPackAdminVO getForAdmin(String targetTenantId);

    void update(String targetTenantId, BrandPackUpdateRequest request);

    /** 清除业务租户覆盖，回退平台默认；平台租户不可 reset */
    void resetOverride(String targetTenantId);
}

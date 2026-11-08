package com.icoolkj.platform.system.role.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.role.dto.RoleCreateRequest;
import com.icoolkj.platform.system.role.dto.RolePageQuery;
import com.icoolkj.platform.system.role.dto.RoleUpdateRequest;
import com.icoolkj.platform.system.role.vo.RoleVO;

/** 角色表服务 */
public interface RoleService {

    PageResult<RoleVO> page(RolePageQuery query);

    RoleVO getById(String id);

    String create(RoleCreateRequest request);

    void update(RoleUpdateRequest request);

    void deleteById(String id);
}

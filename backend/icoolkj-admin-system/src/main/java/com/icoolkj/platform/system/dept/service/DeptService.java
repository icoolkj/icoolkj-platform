package com.icoolkj.platform.system.dept.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.annotation.DataScope;
import com.icoolkj.platform.system.dept.dto.DeptCreateRequest;
import com.icoolkj.platform.system.dept.dto.DeptPageQuery;
import com.icoolkj.platform.system.dept.dto.DeptUpdateRequest;
import com.icoolkj.platform.system.dept.vo.DeptVO;

/** 部门表服务 */
public interface DeptService {

    @DataScope(deptColumn = "id", userColumn = "")
    PageResult<DeptVO> page(DeptPageQuery query);

    DeptVO getById(String id);

    String create(DeptCreateRequest request);

    void update(DeptUpdateRequest request);

    void deleteById(String id);
}

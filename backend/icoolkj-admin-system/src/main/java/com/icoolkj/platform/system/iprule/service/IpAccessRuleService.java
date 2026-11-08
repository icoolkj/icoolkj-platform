package com.icoolkj.platform.system.iprule.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.iprule.dto.IpAccessRuleCreateRequest;
import com.icoolkj.platform.system.iprule.dto.IpAccessRulePageQuery;
import com.icoolkj.platform.system.iprule.dto.IpAccessRuleUpdateRequest;
import com.icoolkj.platform.system.iprule.vo.IpAccessRuleVO;

/** IP访问规则服务 */
public interface IpAccessRuleService {

    PageResult<IpAccessRuleVO> page(IpAccessRulePageQuery query);

    IpAccessRuleVO getById(String id);

    String create(IpAccessRuleCreateRequest request);

    void update(IpAccessRuleUpdateRequest request);

    void deleteById(String id);
}

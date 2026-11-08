package com.icoolkj.platform.infra.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.infra.dto.ThirdPartyCallLogPageQuery;
import com.icoolkj.platform.infra.vo.ThirdPartyCallLogVO;

/** 第三方调用日志服务 */
public interface ThirdPartyCallLogService {

    PageResult<ThirdPartyCallLogVO> page(ThirdPartyCallLogPageQuery query);

    ThirdPartyCallLogVO getById(String id);

}

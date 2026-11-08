package com.icoolkj.platform.infra.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.infra.dto.AsyncTaskLogPageQuery;
import com.icoolkj.platform.infra.vo.AsyncTaskLogVO;

/** 异步任务日志服务 */
public interface AsyncTaskLogService {

    PageResult<AsyncTaskLogVO> page(AsyncTaskLogPageQuery query);

    AsyncTaskLogVO getById(String id);

}

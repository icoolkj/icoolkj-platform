package com.icoolkj.platform.infra.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.annotation.DataScope;
import com.icoolkj.platform.infra.dto.AsyncTaskCreateRequest;
import com.icoolkj.platform.infra.dto.AsyncTaskPageQuery;
import com.icoolkj.platform.infra.dto.AsyncTaskUpdateRequest;
import com.icoolkj.platform.infra.vo.AsyncTaskVO;

/** 异步任务服务 */
public interface AsyncTaskService {

    @DataScope(userColumn = "user_id", deptColumn = "")
    PageResult<AsyncTaskVO> page(AsyncTaskPageQuery query);

    AsyncTaskVO getById(String id);

    /**
     * Submitter-scoped status read: authenticated owner of the task only.
     * Used by Excel async import polling — does not require {@code infra:asyncTask:list}.
     */
    AsyncTaskVO getMineById(String id);

    String create(AsyncTaskCreateRequest request);

    void update(AsyncTaskUpdateRequest request);

    void deleteById(String id);

}

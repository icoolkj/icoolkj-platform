package com.icoolkj.platform.system.operationlog.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.operationlog.dto.OperationLogPageQuery;
import com.icoolkj.platform.system.operationlog.vo.OperationLogVO;

import java.time.LocalDateTime;

/** 操作日志服务 */
public interface OperationLogService {

    PageResult<OperationLogVO> page(OperationLogPageQuery query);

    OperationLogVO getById(String id);

    int purgeBefore(java.time.LocalDateTime before);

    int archiveThenPurge(LocalDateTime before);

    int purgeArchiveBefore(LocalDateTime before);
}

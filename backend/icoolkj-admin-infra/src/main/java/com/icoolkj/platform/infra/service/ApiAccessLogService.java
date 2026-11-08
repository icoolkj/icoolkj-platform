package com.icoolkj.platform.infra.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.infra.dto.ApiAccessLogPageQuery;
import com.icoolkj.platform.infra.vo.ApiAccessLogVO;

import java.time.LocalDateTime;

/** API访问日志服务 */
public interface ApiAccessLogService {

    PageResult<ApiAccessLogVO> page(ApiAccessLogPageQuery query);

    ApiAccessLogVO getById(String id);

    int purgeBefore(java.time.LocalDateTime before);

    int archiveThenPurge(LocalDateTime before);

    int purgeArchiveBefore(LocalDateTime before);
}

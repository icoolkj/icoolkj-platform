package com.icoolkj.platform.system.loginlog.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.loginlog.dto.LoginLogPageQuery;
import com.icoolkj.platform.system.loginlog.vo.LoginLogVO;

import java.time.LocalDateTime;

/** 登录日志：审计写入与查询 */
public interface LoginLogService {

    void recordSuccess(String tenantId, String username, String message, String clientIp, String userAgent);

    void recordFailure(String tenantId, String username, String message, String clientIp, String userAgent);

    PageResult<LoginLogVO> page(LoginLogPageQuery query);

    LoginLogVO getById(String id);

    /** Delete logs strictly before {@code before} (retention job only). */
    int purgeBefore(LocalDateTime before);

    /** Archive then delete live rows older than {@code before}; returns archived count. */
    int archiveThenPurge(LocalDateTime before);

    int purgeArchiveBefore(LocalDateTime before);
}

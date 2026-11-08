package com.icoolkj.platform.infra.convert;

import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.dto.AsyncTaskCreateRequest;
import com.icoolkj.platform.infra.dto.AsyncTaskUpdateRequest;
import com.icoolkj.platform.infra.vo.AsyncTaskVO;

/** 异步任务表对象转换 */
public final class AsyncTaskConvert {

    private AsyncTaskConvert() {
    }

    public static AsyncTaskVO toVo(AsyncTaskEntity entity) {
        if (entity == null) {
            return null;
        }
        AsyncTaskVO vo = new AsyncTaskVO();
        vo.setId(entity.getId());
        vo.setTenantId(entity.getTenantId());
        vo.setTaskType(entity.getTaskType());
        vo.setTaskName(entity.getTaskName());
        vo.setBizType(entity.getBizType());
        vo.setBizId(entity.getBizId());
        vo.setTaskStatus(entity.getTaskStatus());
        vo.setRequestParam(entity.getRequestParam());
        vo.setResultData(entity.getResultData());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setRetryCount(entity.getRetryCount());
        vo.setMaxRetryCount(entity.getMaxRetryCount());
        vo.setRequeueCount(entity.getRequeueCount());
        vo.setStartedAt(entity.getStartedAt());
        vo.setFinishedAt(entity.getFinishedAt());
        vo.setUserId(entity.getUserId());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static AsyncTaskEntity toEntity(AsyncTaskCreateRequest request) {
        AsyncTaskEntity entity = new AsyncTaskEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(request.getTenantId()));
        entity.setTaskType(request.getTaskType());
        entity.setTaskName(request.getTaskName());
        entity.setBizType(request.getBizType());
        entity.setBizId(request.getBizId());
        entity.setTaskStatus(request.getTaskStatus());
        entity.setRequestParam(request.getRequestParam());
        entity.setResultData(request.getResultData());
        entity.setErrorMessage(request.getErrorMessage());
        entity.setRetryCount(request.getRetryCount());
        entity.setMaxRetryCount(request.getMaxRetryCount());
        entity.setRequeueCount(0);
        entity.setStartedAt(request.getStartedAt());
        entity.setFinishedAt(request.getFinishedAt());
        return entity;
    }

    public static void copy(AsyncTaskUpdateRequest request, AsyncTaskEntity entity) {
        entity.setTaskType(request.getTaskType());
        entity.setTaskName(request.getTaskName());
        entity.setBizType(request.getBizType());
        entity.setBizId(request.getBizId());
        entity.setTaskStatus(request.getTaskStatus());
        entity.setRequestParam(request.getRequestParam());
        entity.setResultData(request.getResultData());
        entity.setErrorMessage(request.getErrorMessage());
        entity.setRetryCount(request.getRetryCount());
        entity.setMaxRetryCount(request.getMaxRetryCount());
        entity.setStartedAt(request.getStartedAt());
        entity.setFinishedAt(request.getFinishedAt());
    }
}

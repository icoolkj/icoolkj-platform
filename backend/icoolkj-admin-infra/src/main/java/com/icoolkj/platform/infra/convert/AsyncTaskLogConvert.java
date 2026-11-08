package com.icoolkj.platform.infra.convert;

import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.infra.domain.AsyncTaskLogEntity;
import com.icoolkj.platform.infra.dto.AsyncTaskLogCreateRequest;
import com.icoolkj.platform.infra.dto.AsyncTaskLogUpdateRequest;
import com.icoolkj.platform.infra.vo.AsyncTaskLogVO;

/** 异步任务日志表对象转换 */
public final class AsyncTaskLogConvert {

    private AsyncTaskLogConvert() {
    }

    public static AsyncTaskLogVO toVo(AsyncTaskLogEntity entity) {
        if (entity == null) {
            return null;
        }
        AsyncTaskLogVO vo = new AsyncTaskLogVO();
        vo.setId(entity.getId());
        vo.setTenantId(entity.getTenantId());
        vo.setTaskId(entity.getTaskId());
        vo.setLogLevel(entity.getLogLevel());
        vo.setLogContent(entity.getLogContent());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static AsyncTaskLogEntity toEntity(AsyncTaskLogCreateRequest request) {
        AsyncTaskLogEntity entity = new AsyncTaskLogEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(request.getTenantId()));
        entity.setTaskId(request.getTaskId());
        entity.setLogLevel(request.getLogLevel());
        entity.setLogContent(request.getLogContent());
        return entity;
    }

    public static void copy(AsyncTaskLogUpdateRequest request, AsyncTaskLogEntity entity) {
        entity.setTaskId(request.getTaskId());
        entity.setLogLevel(request.getLogLevel());
        entity.setLogContent(request.getLogContent());
    }
}

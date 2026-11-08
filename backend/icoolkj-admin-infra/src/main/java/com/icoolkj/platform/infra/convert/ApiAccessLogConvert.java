package com.icoolkj.platform.infra.convert;

import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.infra.domain.ApiAccessLogEntity;
import com.icoolkj.platform.infra.dto.ApiAccessLogCreateRequest;
import com.icoolkj.platform.infra.dto.ApiAccessLogUpdateRequest;
import com.icoolkj.platform.infra.vo.ApiAccessLogVO;

/** 接口访问日志表对象转换 */
public final class ApiAccessLogConvert {

    private ApiAccessLogConvert() {
    }

    public static ApiAccessLogVO toVo(ApiAccessLogEntity entity) {
        if (entity == null) {
            return null;
        }
        ApiAccessLogVO vo = new ApiAccessLogVO();
        vo.setId(entity.getId());
        vo.setTenantId(entity.getTenantId());
        vo.setTraceId(entity.getTraceId());
        vo.setRequestUri(entity.getRequestUri());
        vo.setRequestMethod(entity.getRequestMethod());
        vo.setClientIp(entity.getClientIp());
        vo.setUserId(entity.getUserId());
        vo.setUsername(entity.getUsername());
        vo.setRequestParam(entity.getRequestParam());
        vo.setResponseCode(entity.getResponseCode());
        vo.setResponseMessage(entity.getResponseMessage());
        vo.setCostTime(entity.getCostTime());
        vo.setAccessedAt(entity.getAccessedAt());
        return vo;
    }

    public static ApiAccessLogEntity toEntity(ApiAccessLogCreateRequest request) {
        ApiAccessLogEntity entity = new ApiAccessLogEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(request.getTenantId()));
        entity.setTraceId(request.getTraceId());
        entity.setRequestUri(request.getRequestUri());
        entity.setRequestMethod(request.getRequestMethod());
        entity.setClientIp(request.getClientIp());
        entity.setUserId(request.getUserId());
        entity.setUsername(request.getUsername());
        entity.setRequestParam(request.getRequestParam());
        entity.setResponseCode(request.getResponseCode());
        entity.setResponseMessage(request.getResponseMessage());
        entity.setCostTime(request.getCostTime());
        entity.setAccessedAt(request.getAccessedAt());
        return entity;
    }

    public static void copy(ApiAccessLogUpdateRequest request, ApiAccessLogEntity entity) {
        entity.setTraceId(request.getTraceId());
        entity.setRequestUri(request.getRequestUri());
        entity.setRequestMethod(request.getRequestMethod());
        entity.setClientIp(request.getClientIp());
        entity.setUserId(request.getUserId());
        entity.setUsername(request.getUsername());
        entity.setRequestParam(request.getRequestParam());
        entity.setResponseCode(request.getResponseCode());
        entity.setResponseMessage(request.getResponseMessage());
        entity.setCostTime(request.getCostTime());
        entity.setAccessedAt(request.getAccessedAt());
    }
}

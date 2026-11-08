package com.icoolkj.platform.system.operationlog.convert;

import com.icoolkj.platform.system.operationlog.domain.OperationLogEntity;
import com.icoolkj.platform.system.operationlog.vo.OperationLogVO;

/** 操作日志表对象转换（只读查询） */
public final class OperationLogConvert {

    private OperationLogConvert() {
    }

    public static OperationLogVO toVo(OperationLogEntity entity) {
        if (entity == null) {
            return null;
        }
        OperationLogVO vo = new OperationLogVO();
        vo.setId(entity.getId());
        vo.setTraceId(entity.getTraceId());
        vo.setModuleName(entity.getModuleName());
        vo.setBusinessType(entity.getBusinessType());
        vo.setMethod(entity.getMethod());
        vo.setRequestMethod(entity.getRequestMethod());
        vo.setOperatorId(entity.getOperatorId());
        vo.setOperatorName(entity.getOperatorName());
        vo.setOperationUrl(entity.getOperationUrl());
        vo.setOperationIp(entity.getOperationIp());
        vo.setOperationLocation(entity.getOperationLocation());
        vo.setRequestParam(entity.getRequestParam());
        vo.setResponseResult(entity.getResponseResult());
        vo.setOperationStatus(entity.getOperationStatus());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setCostTime(entity.getCostTime());
        vo.setOperationAt(entity.getOperationAt());
        return vo;
    }
}

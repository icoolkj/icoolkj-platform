package com.icoolkj.platform.common.model.log;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogRecord {

    private String tenantId;
    private String traceId;
    private String moduleName;
    private String businessType;
    private String method;
    private String requestMethod;
    private String operatorId;
    private String operatorName;
    private String operationUrl;
    private String operationIp;
    private String operationLocation;
    private String requestParam;
    private String responseResult;
    private String operationStatus;
    private String errorMessage;
    private Integer costTime;
    private LocalDateTime operationAt;
}

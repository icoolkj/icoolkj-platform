package com.icoolkj.platform.system.operationlog.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 操作日志表列表/详情 */
@Data
public class OperationLogVO {
    private String id;
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

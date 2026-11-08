package com.icoolkj.platform.system.operationlog.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 操作日志表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OperationLogPageQuery extends PageQuery {
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

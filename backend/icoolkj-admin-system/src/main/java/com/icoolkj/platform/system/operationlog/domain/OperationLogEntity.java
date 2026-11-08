package com.icoolkj.platform.system.operationlog.domain;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 操作日志表 — maps to `sys_operation_log`. */
@TableName("sys_operation_log")
@Getter
@Setter
public class OperationLogEntity extends TenantBaseEntity {

    @TableField("trace_id")
    private String traceId;

    @TableField("module_name")
    private String moduleName;

    @TableField("business_type")
    private String businessType;

    private String method;

    @TableField("request_method")
    private String requestMethod;

    @TableField("operator_id")
    private String operatorId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("operation_url")
    private String operationUrl;

    @TableField("operation_ip")
    private String operationIp;

    @TableField("operation_location")
    private String operationLocation;

    @TableField("request_param")
    private String requestParam;

    @TableField("response_result")
    private String responseResult;

    @TableField("operation_status")
    private String operationStatus;

    @TableField("error_message")
    private String errorMessage;

    @TableField("cost_time")
    private Integer costTime;

    @TableField("operation_at")
    private LocalDateTime operationAt;

    @TableField("prev_hash")
    private String prevHash;

    @TableField("row_hash")
    private String rowHash;

}

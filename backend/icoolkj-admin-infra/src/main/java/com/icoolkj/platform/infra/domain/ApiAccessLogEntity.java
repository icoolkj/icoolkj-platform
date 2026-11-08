package com.icoolkj.platform.infra.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** API访问日志 — maps to `infra_api_access_log`. */
@TableName("infra_api_access_log")
@Getter
@Setter
public class ApiAccessLogEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("trace_id")
    private String traceId;

    @TableField("request_uri")
    private String requestUri;

    @TableField("request_method")
    private String requestMethod;

    @TableField("client_ip")
    private String clientIp;

    @TableField("user_id")
    private String userId;

    private String username;

    @TableField("request_param")
    private String requestParam;

    @TableField("response_code")
    private Integer responseCode;

    @TableField("response_message")
    private String responseMessage;

    @TableField("cost_time")
    private Integer costTime;

    @TableField("accessed_at")
    private LocalDateTime accessedAt;

    @TableField("prev_hash")
    private String prevHash;

    @TableField("row_hash")
    private String rowHash;

}

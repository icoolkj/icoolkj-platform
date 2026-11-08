package com.icoolkj.platform.infra.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 第三方调用日志 — maps to `infra_thirdparty_call_log`. */
@TableName("infra_thirdparty_call_log")
@Getter
@Setter
public class ThirdPartyCallLogEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("provider_name")
    private String providerName;

    @TableField("biz_type")
    private String bizType;

    @TableField("request_url")
    private String requestUrl;

    @TableField("request_method")
    private String requestMethod;

    @TableField("request_data")
    private String requestData;

    @TableField("response_data")
    private String responseData;

    @TableField("response_status")
    private String responseStatus;

    @TableField("error_message")
    private String errorMessage;

    @TableField("cost_time")
    private Integer costTime;

    @TableField("called_at")
    private LocalDateTime calledAt;

}

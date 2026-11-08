package com.icoolkj.platform.infra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

/** 修改接口访问日志表 */
@Data
public class ApiAccessLogUpdateRequest {

    @NotBlank(message = "{validation.id.required}")
    private String id;
    @NotBlank(message = "{gen.apiAccessLog.tenantId.required}")
    @Size(max = 20, message = "{gen.apiAccessLog.tenantId.maxLength}")
    private String tenantId;
    @Size(max = 64, message = "{gen.apiAccessLog.traceId.maxLength}")
    private String traceId;
    @NotBlank(message = "{gen.apiAccessLog.requestUri.required}")
    @Size(max = 255, message = "{gen.apiAccessLog.requestUri.maxLength}")
    private String requestUri;
    @NotBlank(message = "{gen.apiAccessLog.requestMethod.required}")
    @Size(max = 20, message = "{gen.apiAccessLog.requestMethod.maxLength}")
    private String requestMethod;
    @Size(max = 64, message = "{gen.apiAccessLog.clientIp.maxLength}")
    private String clientIp;
    @Size(max = 20, message = "{gen.apiAccessLog.userId.maxLength}")
    private String userId;
    @Size(max = 50, message = "{gen.apiAccessLog.username.maxLength}")
    private String username;
    @Size(max = 65535, message = "{gen.apiAccessLog.requestParam.maxLength}")
    private String requestParam;
    private Integer responseCode;
    @Size(max = 255, message = "{gen.apiAccessLog.responseMessage.maxLength}")
    private String responseMessage;
    private Integer costTime;
    @NotNull(message = "{gen.apiAccessLog.accessedAt.required}")
    private LocalDateTime accessedAt;
}

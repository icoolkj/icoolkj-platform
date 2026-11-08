package com.icoolkj.platform.infra.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 接口访问日志表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApiAccessLogPageQuery extends PageQuery {
    private String tenantId;
    private String traceId;
    private String requestUri;
    private String requestMethod;
    private String clientIp;
    private String userId;
    private String username;
    private String requestParam;
    private Integer responseCode;
    private String responseMessage;
    private Integer costTime;
    private LocalDateTime accessedAt;
}

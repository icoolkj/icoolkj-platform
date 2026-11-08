package com.icoolkj.platform.generator.dto;

import lombok.Data;

@Data
public class ToolAiInvokeLogPageQuery {
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String tenantId;
    private String bizType;
    private String responseStatus;
    private String operatorName;
}

package com.icoolkj.platform.generator.dto;

import lombok.Data;

@Data
public class ToolAiDdlRecordPageQuery {
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String tenantId;
    private String recordType;
    private String status;
    private String targetTableName;
}

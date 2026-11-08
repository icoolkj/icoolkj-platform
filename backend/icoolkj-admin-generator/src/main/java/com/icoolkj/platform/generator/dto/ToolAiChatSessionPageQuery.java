package com.icoolkj.platform.generator.dto;

import lombok.Data;

@Data
public class ToolAiChatSessionPageQuery {
    private Integer pageNo = 1;
    private Integer pageSize = 20;
    private String tenantId;
    private String title;
    private String operatorId;
    /** true=仅当前用户（对话页）；false=审计列表 */
    private Boolean mineOnly = true;
}

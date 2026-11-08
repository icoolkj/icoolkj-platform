package com.icoolkj.platform.workflow.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 工作流任务分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WorkflowTaskPageQuery extends PageQuery {

    private String bizTitle;
    private String bizType;
    private String processDefinitionKey;
    private String taskName;
    private LocalDateTime startTimeBegin;
    private LocalDateTime startTimeEnd;
}

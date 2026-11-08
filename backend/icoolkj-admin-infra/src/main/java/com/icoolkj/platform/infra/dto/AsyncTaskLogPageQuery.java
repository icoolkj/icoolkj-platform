package com.icoolkj.platform.infra.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 异步任务日志表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AsyncTaskLogPageQuery extends PageQuery {
    private String tenantId;
    private String taskId;
    private String logLevel;
    private String logContent;
}

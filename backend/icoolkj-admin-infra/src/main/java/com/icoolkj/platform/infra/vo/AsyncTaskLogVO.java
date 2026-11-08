package com.icoolkj.platform.infra.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 异步任务日志表列表/详情 */
@Data
public class AsyncTaskLogVO {
    private String id;
    private String tenantId;
    private String taskId;
    private String logLevel;
    private String logContent;
    private LocalDateTime createdAt;
}

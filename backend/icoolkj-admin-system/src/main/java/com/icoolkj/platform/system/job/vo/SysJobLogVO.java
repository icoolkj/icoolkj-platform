package com.icoolkj.platform.system.job.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SysJobLogVO {
    private String id;
    private String jobId;
    private String jobCode;
    private String triggerType;
    private String triggerUserId;
    private String status;
    private String jobMessage;
    private String exceptionInfo;
    private String requestParam;
    private String resultSummary;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
}

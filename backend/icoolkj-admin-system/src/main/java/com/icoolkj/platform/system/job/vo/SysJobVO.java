package com.icoolkj.platform.system.job.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SysJobVO {
    private String id;
    private String jobCode;
    private String jobName;
    private String jobGroup;
    private String cronExpression;
    private String misfirePolicy;
    private String allowConcurrent;
    private String status;
    private String remark;
    private String lastStatus;
    private String lastMessage;
    private String lastTriggerAt;
    private String lastStartedAt;
    private String lastFinishedAt;
    private LocalDateTime updatedAt;
}

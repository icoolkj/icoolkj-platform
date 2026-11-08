package com.icoolkj.platform.workflow.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** One start/reject/approve cycle for the same business document. */
@Data
public class WorkflowProcessCycleVO {

    private Integer cycleNo;
    private String processInstanceId;
    /** running | approved | rejected | cancelled */
    private String status;
    private String starterUserId;
    private String starterUserName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

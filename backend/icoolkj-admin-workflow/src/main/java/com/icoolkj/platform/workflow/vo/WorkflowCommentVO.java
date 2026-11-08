package com.icoolkj.platform.workflow.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 审批意见 */
@Data
public class WorkflowCommentVO {

    private String userId;
    private String userName;
    private String message;
    private LocalDateTime time;
}

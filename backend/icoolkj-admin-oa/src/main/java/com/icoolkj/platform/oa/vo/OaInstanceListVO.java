package com.icoolkj.platform.oa.vo;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 在线办公列表行（跨业务） */
@Getter
@Setter
public class OaInstanceListVO {

    private String id;
    private String categoryCode;
    private String instanceTitle;
    private String approveStatus;
    private String processInstanceId;
    private String starterUserId;
    private LocalDateTime submittedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String remark;
    /** True once any workflow cycle exists for this document. */
    private Boolean hasApprovalHistory;
    /** 运行态 behavior.allowWithdraw（版本快照）；非 running 为 false */
    private Boolean allowWithdraw;
}

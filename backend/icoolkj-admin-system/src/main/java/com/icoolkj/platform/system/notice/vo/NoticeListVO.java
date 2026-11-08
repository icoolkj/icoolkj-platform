package com.icoolkj.platform.system.notice.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 通知公告列表项（不含富文本正文） */
@Data
public class NoticeListVO {
    private String id;
    private String noticeTitle;
    private String noticeType;
    private String coverFileId;
    private String coverPreviewUrl;
    private String noticeSummary;
    private Integer sortNo;
    private String publishLocations;
    private String status;
    /** 审批状态：draft/running/approved/rejected/cancelled/returned/closed */
    private String approveStatus;
    /** 上架状态：unpublished/published */
    private String releaseStatus;
    private String processInstanceId;
    private String remark;
    private LocalDateTime createdAt;
    /** 一旦存在任意审批周期，禁删仅可关闭 */
    private Boolean hasApprovalHistory;
    /** 运行态 behavior.allowWithdraw；非 running 为 false */
    private Boolean allowWithdraw;
}

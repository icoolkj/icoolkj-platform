package com.icoolkj.platform.system.notice.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 通知公告表列表/详情 */
@Data
public class NoticeVO {
    private String id;
    private String noticeTitle;
    private String noticeType;
    private String coverFileId;
    private String coverPreviewUrl;
    private String noticeContent;
    private String noticeSummary;
    private Integer sortNo;
    private String publishLocations;
    private String status;
    private String approveStatus;
    private String releaseStatus;
    private String processInstanceId;
    private String starterUserId;
    private LocalDateTime submittedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime publishedAt;
    private String remark;
    private LocalDateTime createdAt;
}

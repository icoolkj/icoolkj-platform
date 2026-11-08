package com.icoolkj.platform.system.notice.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 已发布公告摘要（工作台/门户展示） */
@Data
public class NoticePublishedVO {
    private String id;
    private String noticeTitle;
    private String noticeType;
    private String coverFileId;
    private String coverPreviewUrl;
    private String noticeSummary;
    private String noticeContent;
    private Integer sortNo;
    private LocalDateTime createdAt;
}

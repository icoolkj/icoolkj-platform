package com.icoolkj.platform.system.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 新增通知公告表 */
@Data
public class NoticeCreateRequest {
    @NotBlank(message = "{gen.notice.noticeTitle.required}")
    @Size(max = 200, message = "{gen.notice.noticeTitle.maxLength}")
    private String noticeTitle;
    @NotBlank(message = "{gen.notice.noticeType.required}")
    @Size(max = 1, message = "{gen.notice.noticeType.maxLength}")
    private String noticeType;
    @Size(max = 20, message = "{gen.notice.coverFileId.maxLength}")
    private String coverFileId;
    @NotBlank(message = "{gen.notice.noticeContent.required}")
    @Size(max = 65535, message = "{gen.notice.noticeContent.maxLength}")
    private String noticeContent;
    @NotBlank(message = "{gen.notice.noticeSummary.required}")
    @Size(max = 60, message = "{gen.notice.noticeSummary.maxLength}")
    private String noticeSummary;
    @NotNull(message = "{gen.notice.sortNo.required}")
    private Integer sortNo;
    @NotBlank(message = "{gen.notice.publishLocations.required}")
    @Size(max = 100, message = "{gen.notice.publishLocations.maxLength}")
    private String publishLocations;
    @NotBlank(message = "{gen.notice.status.required}")
    @Size(max = 1, message = "{gen.notice.status.maxLength}")
    private String status;
    @Size(max = 500, message = "{gen.notice.remark.maxLength}")
    private String remark;
}

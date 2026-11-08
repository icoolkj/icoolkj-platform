package com.icoolkj.platform.system.notice.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 通知公告表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NoticePageQuery extends PageQuery {
    private String noticeTitle;
    private String noticeType;
    private String publishLocation;
    private String status;
    private String approveStatus;
    private String releaseStatus;
}

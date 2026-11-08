package com.icoolkj.platform.system.sitemessage.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

/** 个人收件箱分页查询 */
@Getter
@Setter
public class SiteMessageInboxPageQuery extends PageQuery {

    private String tenantId;

    private String userId;

    /** system / business */
    private String messageCategory;

    /** 0 未读 / 1 已读 */
    private String readStatus;
}

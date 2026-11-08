package com.icoolkj.platform.system.sitemessage.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

/** 管理端站内信查收情况分页查询 */
@Getter
@Setter
public class SiteMessageRecipientPageQuery extends PageQuery {

    /** 服务端绑定 */
    private String messageId;

    /** 已读状态：0 未读 / 1 已读 */
    private String readStatus;
}

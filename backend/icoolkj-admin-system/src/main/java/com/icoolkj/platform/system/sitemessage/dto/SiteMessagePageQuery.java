package com.icoolkj.platform.system.sitemessage.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

/** 管理端站内信分页查询 */
@Getter
@Setter
public class SiteMessagePageQuery extends PageQuery {

    private String tenantId;

    private String messageSubject;

    private String messageCategory;

    /** 服务端绑定：仅查询当前操作人发送的记录 */
    private String senderId;
}

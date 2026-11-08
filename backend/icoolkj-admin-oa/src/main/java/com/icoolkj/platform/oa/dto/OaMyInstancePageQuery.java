package com.icoolkj.platform.oa.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

/** 在线办公：我的草稿 / 已发申请 */
@Getter
@Setter
public class OaMyInstancePageQuery extends PageQuery {

    /** draft | submitted */
    private String scope;
    private String instanceTitle;
    private String categoryCode;
    private String approveStatus;
    /** 服务端强制写入，前端勿传 */
    private String starterUserId;
}

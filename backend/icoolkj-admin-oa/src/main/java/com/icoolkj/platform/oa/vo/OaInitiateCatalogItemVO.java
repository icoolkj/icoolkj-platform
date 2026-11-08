package com.icoolkj.platform.oa.vo;

import lombok.Getter;
import lombok.Setter;

/** 发起中心卡片项（可发起的已启用+已发布租户模板） */
@Getter
@Setter
public class OaInitiateCatalogItemVO {

    private String tenantTemplateId;
    private String categoryCode;
    private String templateCode;
    private String templateName;
    private String publishedTplVerId;
    private Integer publishedVersionNo;
    private String remark;
}

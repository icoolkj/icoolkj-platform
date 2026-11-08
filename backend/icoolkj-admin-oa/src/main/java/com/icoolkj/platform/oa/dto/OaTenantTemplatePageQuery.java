package com.icoolkj.platform.oa.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OaTenantTemplatePageQuery extends PageQuery {

    private String categoryCode;
    private String templateCode;
    private String templateName;
    private String enableStatus;
    private String status;
}

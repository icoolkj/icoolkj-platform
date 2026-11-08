package com.icoolkj.platform.oa.vo;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OaTenantTemplateDetailVO extends OaTenantTemplateVO {

    private OaTemplateVersionVO draftVersion;
    private OaTemplateVersionVO publishedVersion;
    private List<OaTemplateVersionVO> versions;
    private String platformTemplateName;
}

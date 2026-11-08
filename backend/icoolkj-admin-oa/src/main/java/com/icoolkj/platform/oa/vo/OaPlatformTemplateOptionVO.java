package com.icoolkj.platform.oa.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OaPlatformTemplateOptionVO {

    private String id;
    private String categoryCode;
    private String templateCode;
    private String templateName;
    private Integer latestVersionNo;
    private String publishedVersionId;
}

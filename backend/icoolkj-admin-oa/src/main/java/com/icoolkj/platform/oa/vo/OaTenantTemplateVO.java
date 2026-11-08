package com.icoolkj.platform.oa.vo;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OaTenantTemplateVO {

    private String id;
    private String categoryCode;
    private String templateCode;
    private String templateName;
    private String platformTemplateId;
    private String sourcePlatformTplVerId;
    private Integer latestVersionNo;
    private String publishedTplVerId;
    private String enableStatus;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** 列表态：是否存在未发布草稿 */
    private Boolean hasDraft;
    /** 列表态：草稿版本号 */
    private Integer draftVersionNo;
    /** 列表态：平台模板最新已发布版本号 */
    private Integer platformLatestVersionNo;
    /** 列表态：是否落后于平台已发布版本（需同步） */
    private Boolean platformOutdated;
}

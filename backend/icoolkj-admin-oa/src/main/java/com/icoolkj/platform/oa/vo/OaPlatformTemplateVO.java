package com.icoolkj.platform.oa.vo;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OaPlatformTemplateVO {

    private String id;
    private String categoryCode;
    private String templateCode;
    private String templateName;
    private String wfModelId;
    private Integer latestVersionNo;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** 列表态：是否存在未发布草稿 */
    private Boolean hasDraft;
    /** 列表态：草稿版本号 */
    private Integer draftVersionNo;
}

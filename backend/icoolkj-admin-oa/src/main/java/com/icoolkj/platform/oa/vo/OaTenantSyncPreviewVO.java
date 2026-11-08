package com.icoolkj.platform.oa.vo;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** OA 租户同步平台预览 */
@Getter
@Setter
public class OaTenantSyncPreviewVO {

    private String templateId;
    private String templateName;
    private Integer platformLatestVersionNo;
    private String platformPublishedVersionId;
    private String currentSourcePlatformTplVerId;
    private Integer currentTenantVersionNo;
    private String currentTenantVersionStatus;
    private Boolean hasChanges;
    private Integer addedCount;
    private Integer removedCount;
    private Integer changedCount;
    private Integer keptCustomCount;
    private List<OaTenantFieldSyncDiffVO> diffs = new ArrayList<>();
}

package com.icoolkj.platform.oa.vo;

import lombok.Getter;
import lombok.Setter;

/** OA 租户同步平台字段差异项 */
@Getter
@Setter
public class OaTenantFieldSyncDiffVO {

    /** ADDED / REMOVED / CHANGED / KEPT_CUSTOM */
    private String changeType;
    private String fieldCode;
    private String fieldName;
    /** 当前租户侧摘要（新增时为空） */
    private String currentSummary;
    /** 同步后摘要（删除时为空） */
    private String afterSummary;
}

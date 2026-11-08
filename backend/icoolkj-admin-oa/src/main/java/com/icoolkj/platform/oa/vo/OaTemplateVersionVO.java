package com.icoolkj.platform.oa.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OaTemplateVersionVO {

    private String id;
    private String templateId;
    private Integer versionNo;
    private String versionStatus;
    private String wfModelId;
    private Integer wfModelVersionNo;
    private String platformTplVerId;
    private LocalDateTime publishedAt;
    private String remark;
    private LocalDateTime createdAt;
    private List<OaFieldDefVO> fields;
    /** 草稿布局 FormDocument JSON */
    private String layoutJson;
    /** 租户已发布版本的布局快照（平台版本无此字段） */
    private String layoutSnapshotJson;
}

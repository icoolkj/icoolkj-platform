package com.icoolkj.platform.oa.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@TableName("oa_tenant_template_version")
@Getter
@Setter
public class OaTenantTemplateVersionEntity extends TenantBaseEntity {

    @TableField("tenant_template_id")
    private String tenantTemplateId;

    @TableField("version_no")
    private Integer versionNo;

    @TableField("version_status")
    private String versionStatus;

    @TableField("platform_tpl_ver_id")
    private String platformTplVerId;

    @TableField("wf_model_id")
    private String wfModelId;

    @TableField("wf_model_version_no")
    private Integer wfModelVersionNo;

    @TableField("field_snapshot_json")
    private String fieldSnapshotJson;

    /** 草稿布局 FormDocument JSON */
    @TableField("layout_json")
    private String layoutJson;

    /** 发布时布局快照 */
    @TableField("layout_snapshot_json")
    private String layoutSnapshotJson;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    private String remark;
}

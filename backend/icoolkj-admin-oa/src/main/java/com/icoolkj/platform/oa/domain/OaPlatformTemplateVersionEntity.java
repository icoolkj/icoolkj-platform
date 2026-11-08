package com.icoolkj.platform.oa.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@TableName("oa_platform_template_version")
@Getter
@Setter
public class OaPlatformTemplateVersionEntity extends TenantBaseEntity {

    @TableField("template_id")
    private String templateId;

    @TableField("version_no")
    private Integer versionNo;

    @TableField("version_status")
    private String versionStatus;

    @TableField("wf_model_id")
    private String wfModelId;

    @TableField("wf_model_version_no")
    private Integer wfModelVersionNo;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    private String remark;

    /** FormDocument JSON（草稿/已发布版本均存于此列） */
    @TableField("layout_json")
    private String layoutJson;
}

package com.icoolkj.platform.oa.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** OA 平台审批模板 */
@TableName("oa_platform_template")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaPlatformTemplateEntity extends TenantBaseEntity {

    @TableField("category_code")
    private String categoryCode;

    @TableField("template_code")
    private String templateCode;

    @TableField("template_name")
    private String templateName;

    @TableField("wf_model_id")
    private String wfModelId;

    @TableField("latest_version_no")
    private Integer latestVersionNo;

    private String status;
    private String remark;
}

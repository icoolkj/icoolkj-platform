package com.icoolkj.platform.oa.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** OA 租户审批模板映射 */
@TableName("oa_tenant_template")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaTenantTemplateEntity extends TenantBaseEntity {

    @TableField("category_code")
    private String categoryCode;

    @TableField("template_code")
    private String templateCode;

    @TableField("template_name")
    private String templateName;

    @TableField("platform_template_id")
    private String platformTemplateId;

    @TableField("source_platform_tpl_ver_id")
    private String sourcePlatformTplVerId;

    @TableField("latest_version_no")
    private Integer latestVersionNo;

    @TableField("published_tpl_ver_id")
    private String publishedTplVerId;

    @TableField("enable_status")
    private String enableStatus;

    private String status;
    private String remark;
}

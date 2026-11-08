package com.icoolkj.platform.infra.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 附件测试业务 — maps to {@code infra_attachment_demo}. */
@TableName("infra_attachment_demo")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AttachmentDemoEntity extends TenantBaseEntity {

    @TableField("title")
    private String title;

    /** profile_code → fileId 或 fileId[] JSON */
    @TableField("assets_json")
    private String assetsJson;

    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;
}

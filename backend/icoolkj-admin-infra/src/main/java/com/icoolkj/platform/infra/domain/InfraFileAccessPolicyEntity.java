package com.icoolkj.platform.infra.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 文件访问策略 — maps to {@code infra_file_access_policy}. */
@TableName("infra_file_access_policy")
@Getter
@Setter
public class InfraFileAccessPolicyEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("biz_type_pattern")
    private String bizTypePattern;

    @TableField("auth_mode")
    private String authMode;

    @TableField("tenant_scope")
    private String tenantScope;

    private String visibility;

    @TableField("anon_purposes")
    private String anonPurposes;

    @TableField("perm_any_of")
    private String permAnyOf;

    @TableField("sort_no")
    private Integer sortNo;

    private String enabled;

    private String remark;

    @TableField("created_by")
    private String createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_by")
    private String updatedBy;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    private Integer deleted;
}

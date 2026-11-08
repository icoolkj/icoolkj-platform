package com.icoolkj.platform.infra.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 文件上传槽位 — maps to {@code infra_file_profile}. */
@TableName("infra_file_profile")
@Getter
@Setter
public class InfraFileProfileEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("profile_code")
    private String profileCode;

    @TableField("profile_name")
    private String profileName;

    @TableField("profile_name_i18n")
    private String profileNameI18n;

    @TableField("category_tag")
    private String categoryTag;

    private String status;

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

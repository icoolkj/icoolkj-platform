package com.icoolkj.platform.infra.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 文件上传槽位规则 — maps to {@code infra_file_profile_rule}. */
@TableName("infra_file_profile_rule")
@Getter
@Setter
public class InfraFileProfileRuleEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("profile_code")
    private String profileCode;

    @TableField("allowed_exts")
    private String allowedExts;

    @TableField("allowed_content_types")
    private String allowedContentTypes;

    private String required;

    @TableField("min_num")
    private Integer minNum;

    @TableField("max_num")
    private Integer maxNum;

    @TableField("min_size_bytes")
    private Long minSizeBytes;

    @TableField("max_size_bytes")
    private Long maxSizeBytes;

    @TableField("sort_no")
    private Integer sortNo;

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

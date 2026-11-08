package com.icoolkj.platform.infra.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 文件材料目录项 — maps to {@code infra_file_catalog_item}. */
@TableName("infra_file_catalog_item")
@Getter
@Setter
public class InfraFileCatalogItemEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("catalog_code")
    private String catalogCode;

    @TableField("profile_code")
    private String profileCode;

    private String required;

    @TableField("min_num")
    private Integer minNum;

    @TableField("max_num")
    private Integer maxNum;

    @TableField("sort_no")
    private Integer sortNo;

    private String status;

    /** role → priority JSON，如 {"sidebar_logo":10} */
    @TableField("chrome_roles")
    private String chromeRoles;

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

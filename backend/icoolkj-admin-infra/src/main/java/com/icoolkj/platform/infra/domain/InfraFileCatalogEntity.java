package com.icoolkj.platform.infra.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 文件材料目录 — maps to {@code infra_file_catalog}. */
@TableName("infra_file_catalog")
@Getter
@Setter
public class InfraFileCatalogEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("catalog_code")
    private String catalogCode;

    @TableField("catalog_name")
    private String catalogName;

    @TableField("catalog_name_i18n")
    private String catalogNameI18n;

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

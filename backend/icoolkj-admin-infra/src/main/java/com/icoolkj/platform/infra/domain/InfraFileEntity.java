package com.icoolkj.platform.infra.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 文件 — maps to `infra_file`。归属用户 {@code user_id}=上传人；审计走基类五元字段。 */
@TableName("infra_file")
@Getter
@Setter
public class InfraFileEntity extends TenantBaseEntity {

    @TableField("biz_type")
    private String bizType;

    @TableField("biz_id")
    private String bizId;

    @TableField("original_name")
    private String originalName;

    @TableField("file_name")
    private String fileName;

    @TableField("file_ext")
    private String fileExt;

    @TableField("content_type")
    private String contentType;

    @TableField("file_size")
    private Long fileSize;

    @TableField("storage_type")
    private String storageType;

    @TableField("bucket_name")
    private String bucketName;

    @TableField("object_key")
    private String objectKey;

    @TableField("file_url")
    private String fileUrl;

    private String md5;

    @TableField("scan_status")
    private String scanStatus;

    @TableField("scan_engine")
    private String scanEngine;

    @TableField("scan_at")
    private LocalDateTime scanAt;

    /** 应用层加密 0否 1是 */
    private String encrypted;

    @TableField("sensitivity_level")
    private String sensitivityLevel;

    /** 归属用户（上传人），L3 数据范围列 */
    @TableField("user_id")
    private String userId;

    private String remark;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

}

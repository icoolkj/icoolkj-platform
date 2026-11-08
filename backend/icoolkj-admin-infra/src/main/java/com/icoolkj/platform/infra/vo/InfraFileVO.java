package com.icoolkj.platform.infra.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 文件列表/详情 */
@Data
public class InfraFileVO {

    private String id;
    private String tenantId;
    private String bizType;
    private String bizId;
    private String originalName;
    private String fileName;
    private String fileExt;
    private String contentType;
    private Long fileSize;
    private String storageType;
    private String bucketName;
    private String objectKey;
    private String fileUrl;
    private String md5;
    private String scanStatus;
    private String scanEngine;
    private LocalDateTime scanAt;
    private String encrypted;
    private String sensitivityLevel;
    /** 归属用户（上传人） */
    private String userId;
    private LocalDateTime createdAt;
    private String remark;

    /** Human-readable size, e.g. 353.45 KB */
    private String fileSizeDisplay;
    /** Active business references count */
    private long referenceCount;
    /** Whether delete is allowed from file admin */
    private boolean deletable;
    /** Whether inline preview is supported */
    private boolean previewable;
    /** Whether current caller may download (FileAccessGuard) */
    private boolean downloadable;

}


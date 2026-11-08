package com.icoolkj.platform.infra.vo;

import lombok.Data;

@Data
public class DatabaseBackupRunResultVO {

    private boolean success;
    private String fileName;
    private Long sizeBytes;
    private String message;
}

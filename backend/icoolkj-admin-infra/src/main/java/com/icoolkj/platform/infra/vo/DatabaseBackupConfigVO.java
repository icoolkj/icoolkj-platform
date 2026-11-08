package com.icoolkj.platform.infra.vo;

import lombok.Data;

/** Read-only backup settings for ops UI. */
@Data
public class DatabaseBackupConfigVO {

    private boolean enabled;
    private String cron;
    private String outputDir;
    private int retentionDays;
    private String mysqldumpCommand;
    private boolean compress;
}

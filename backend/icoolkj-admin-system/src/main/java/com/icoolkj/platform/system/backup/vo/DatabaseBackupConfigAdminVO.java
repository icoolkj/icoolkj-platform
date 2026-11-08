package com.icoolkj.platform.system.backup.vo;

import lombok.Data;

/** Backup settings for admin UI (sys_config + read-only mysqldump from YAML). */
@Data
public class DatabaseBackupConfigAdminVO {

    private boolean enabled;
    private String cron;
    private String outputDir;
    private int retentionDays;
    private String mysqldumpCommand;
    private boolean compress;
    /** When true, mysqldump path is managed in application.yml only. */
    private boolean mysqldumpFromYaml = true;
}

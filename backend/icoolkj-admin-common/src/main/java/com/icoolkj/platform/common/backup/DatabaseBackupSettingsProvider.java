package com.icoolkj.platform.common.backup;

/** Backup settings: schedule from {@code sys_job}; output/retention/compress from sys_config or YAML. */
public interface DatabaseBackupSettingsProvider {

    boolean isEnabled();

    String getCron();

    String getOutputDir();

    int getRetentionDays();

    boolean isCompress();
}

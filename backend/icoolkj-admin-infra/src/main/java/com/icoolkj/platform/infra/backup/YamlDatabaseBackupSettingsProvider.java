package com.icoolkj.platform.infra.backup;

import com.icoolkj.platform.common.backup.DatabaseBackupSettingsProvider;

class YamlDatabaseBackupSettingsProvider implements DatabaseBackupSettingsProvider {

    private final DatabaseBackupProperties properties;

    YamlDatabaseBackupSettingsProvider(DatabaseBackupProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String getCron() {
        return "";
    }

    @Override
    public String getOutputDir() {
        return properties.getOutputDir();
    }

    @Override
    public int getRetentionDays() {
        return properties.getRetentionDays();
    }

    @Override
    public boolean isCompress() {
        return properties.isCompress();
    }
}

package com.icoolkj.platform.system.backup.service.impl;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "icoolkj.backup")
public class YamlDatabaseBackupSettingsFallback {

    private String outputDir = "./data/db-backup";
    private int retentionDays = 14;
    private boolean compress = true;
    private String mysqldumpCommand = "mysqldump";

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public String getMysqldumpCommand() {
        return mysqldumpCommand;
    }

    public void setMysqldumpCommand(String mysqldumpCommand) {
        this.mysqldumpCommand = mysqldumpCommand;
    }
}

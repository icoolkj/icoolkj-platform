package com.icoolkj.platform.infra.backup;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "icoolkj.backup")
public class DatabaseBackupProperties {

    private String outputDir = "./data/db-backup";

    private int retentionDays = 14;

    private String mysqldumpCommand = "mysqldump";

    private boolean compress = true;
}

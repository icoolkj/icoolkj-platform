package com.icoolkj.platform.system.backup.config;

import com.icoolkj.platform.system.backup.service.impl.YamlDatabaseBackupSettingsFallback;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(YamlDatabaseBackupSettingsFallback.class)
public class SystemBackupConfiguration {
}

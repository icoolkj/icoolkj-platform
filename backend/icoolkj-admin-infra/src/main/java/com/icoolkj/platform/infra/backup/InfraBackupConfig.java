package com.icoolkj.platform.infra.backup;

import com.icoolkj.platform.common.backup.DatabaseBackupSettingsProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DatabaseBackupProperties.class)
public class InfraBackupConfig {

    @Bean
    @ConditionalOnMissingBean(DatabaseBackupSettingsProvider.class)
    DatabaseBackupSettingsProvider yamlDatabaseBackupSettingsProvider(DatabaseBackupProperties properties) {
        return new YamlDatabaseBackupSettingsProvider(properties);
    }
}

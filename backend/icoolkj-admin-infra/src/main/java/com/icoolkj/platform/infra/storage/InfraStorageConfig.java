package com.icoolkj.platform.infra.storage;

import com.icoolkj.platform.common.storage.FileStorageSettingsProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfraStorageConfig {

    @Bean
    @ConditionalOnMissingBean(FileStorageSettingsProvider.class)
    FileStorageSettingsProvider yamlFileStorageSettingsProvider(StorageProperties properties) {
        return new YamlFileStorageSettingsProvider(properties);
    }
}

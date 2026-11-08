package com.icoolkj.platform.infra.config;

import com.icoolkj.platform.infra.async.AsyncProperties;
import com.icoolkj.platform.infra.backup.InfraBackupConfig;
import com.icoolkj.platform.infra.provider.ProviderProperties;
import com.icoolkj.platform.infra.storage.InfraStorageConfig;
import com.icoolkj.platform.infra.storage.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({InfraBackupConfig.class, InfraStorageConfig.class})
@EnableConfigurationProperties({
        StorageProperties.class,
        AsyncProperties.class,
        ProviderProperties.class
})
public class InfraAutoConfiguration {

}

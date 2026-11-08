package com.icoolkj.platform.infra.storage;

import com.icoolkj.platform.common.storage.AvatarSettingsProvider;
import com.icoolkj.platform.common.storage.FileStoragePolicyUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class InfraAvatarConfig {

    @Bean
    @ConditionalOnMissingBean(AvatarSettingsProvider.class)
    AvatarSettingsProvider yamlAvatarSettingsProvider() {
        return new YamlAvatarSettingsProvider();
    }

    static final class YamlAvatarSettingsProvider implements AvatarSettingsProvider {

        @Override
        public long getMaxSizeMb() {
            return 2L;
        }

        @Override
        public List<String> getAllowedExtensions() {
            return FileStoragePolicyUtils.splitCsv("jpg,jpeg,png,webp,gif");
        }

        @Override
        public int getOutputSize() {
            return 200;
        }

        @Override
        public int getThumbSmSize() {
            return 32;
        }

        @Override
        public int getThumbMdSize() {
            return 88;
        }

        @Override
        public boolean isContentScanEnabled() {
            return true;
        }
    }
}

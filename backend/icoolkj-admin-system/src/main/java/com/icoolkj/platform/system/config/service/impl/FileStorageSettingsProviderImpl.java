package com.icoolkj.platform.system.config.service.impl;

import com.icoolkj.platform.common.constant.ConfigKeys;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.storage.FileStoragePolicyUtils;
import com.icoolkj.platform.common.storage.FileStorageSettingsProvider;
import com.icoolkj.platform.system.config.service.ConfigService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
public class FileStorageSettingsProviderImpl implements FileStorageSettingsProvider {

    private static final String YAML_DEFAULT_PROVIDER = "local";
    private static final String YAML_ENABLED_PROVIDERS = "local,minio";
    private static final int YAML_MAX_FILE_SIZE_MB = 100;
    private static final String YAML_ALLOWED_EXTENSIONS = "";
    private static final String YAML_BLOCKED_EXTENSIONS = "exe,bat,sh,cmd,com,msi";

    private final ConfigService configService;

    public FileStorageSettingsProviderImpl(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public String getDefaultProvider() {
        return configService.getString(
                TenantConstants.PLATFORM_TENANT_ID,
                ConfigKeys.STORAGE_DEFAULT_PROVIDER,
                YAML_DEFAULT_PROVIDER
        );
    }

    @Override
    public List<String> getEnabledProviders() {
        String raw = configService.getString(
                TenantConstants.PLATFORM_TENANT_ID,
                ConfigKeys.STORAGE_ENABLED_PROVIDERS,
                YAML_ENABLED_PROVIDERS
        );
        return FileStoragePolicyUtils.splitCsv(raw);
    }

    @Override
    public long getMaxFileSizeMb() {
        return configService.getInt(
                TenantConstants.PLATFORM_TENANT_ID,
                ConfigKeys.STORAGE_MAX_FILE_SIZE_MB,
                YAML_MAX_FILE_SIZE_MB
        );
    }

    @Override
    public List<String> getAllowedExtensions() {
        String raw = configService.getString(
                TenantConstants.PLATFORM_TENANT_ID,
                ConfigKeys.STORAGE_ALLOWED_EXTENSIONS,
                YAML_ALLOWED_EXTENSIONS
        );
        return FileStoragePolicyUtils.splitCsv(raw);
    }

    @Override
    public List<String> getBlockedExtensions() {
        String raw = configService.getString(
                TenantConstants.PLATFORM_TENANT_ID,
                ConfigKeys.STORAGE_BLOCKED_EXTENSIONS,
                YAML_BLOCKED_EXTENSIONS
        );
        return FileStoragePolicyUtils.splitCsv(raw);
    }
}

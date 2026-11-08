package com.icoolkj.platform.system.config.service.impl;

import com.icoolkj.platform.common.constant.ConfigKeys;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.storage.AvatarSettingsProvider;
import com.icoolkj.platform.common.storage.FileStoragePolicyUtils;
import com.icoolkj.platform.system.config.service.ConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvatarSettingsProviderImpl implements AvatarSettingsProvider {

    private static final long DEFAULT_MAX_SIZE_MB = 2L;
    private static final String DEFAULT_ALLOWED_EXTENSIONS = "jpg,jpeg,png,webp,gif";
    private static final int DEFAULT_OUTPUT_SIZE = 200;
    private static final int DEFAULT_THUMB_SM = 32;
    private static final int DEFAULT_THUMB_MD = 88;

    private final ConfigService configService;

    public AvatarSettingsProviderImpl(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public long getMaxSizeMb() {
        return configService.getInt(
                TenantConstants.PLATFORM_TENANT_ID,
                ConfigKeys.AVATAR_MAX_SIZE_MB,
                (int) DEFAULT_MAX_SIZE_MB
        );
    }

    @Override
    public List<String> getAllowedExtensions() {
        String raw = configService.getString(
                TenantConstants.PLATFORM_TENANT_ID,
                ConfigKeys.AVATAR_ALLOWED_EXTENSIONS,
                DEFAULT_ALLOWED_EXTENSIONS
        );
        return FileStoragePolicyUtils.splitCsv(raw);
    }

    @Override
    public int getOutputSize() {
        return configService.getInt(
                TenantConstants.PLATFORM_TENANT_ID,
                ConfigKeys.AVATAR_OUTPUT_SIZE,
                DEFAULT_OUTPUT_SIZE
        );
    }

    @Override
    public int getThumbSmSize() {
        return configService.getInt(
                TenantConstants.PLATFORM_TENANT_ID,
                ConfigKeys.AVATAR_THUMB_SM_SIZE,
                DEFAULT_THUMB_SM
        );
    }

    @Override
    public int getThumbMdSize() {
        return configService.getInt(
                TenantConstants.PLATFORM_TENANT_ID,
                ConfigKeys.AVATAR_THUMB_MD_SIZE,
                DEFAULT_THUMB_MD
        );
    }

    @Override
    public boolean isContentScanEnabled() {
        return configService.getBoolean(
                TenantConstants.PLATFORM_TENANT_ID,
                ConfigKeys.AVATAR_CONTENT_SCAN_ENABLED,
                true
        );
    }
}

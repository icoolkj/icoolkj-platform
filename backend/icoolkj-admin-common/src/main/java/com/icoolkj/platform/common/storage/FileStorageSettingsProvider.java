package com.icoolkj.platform.common.storage;

import java.util.List;

/** Effective file storage policy (sys_config with YAML fallback). */
public interface FileStorageSettingsProvider {

    String getDefaultProvider();

    List<String> getEnabledProviders();

    long getMaxFileSizeMb();

    List<String> getAllowedExtensions();

    List<String> getBlockedExtensions();
}

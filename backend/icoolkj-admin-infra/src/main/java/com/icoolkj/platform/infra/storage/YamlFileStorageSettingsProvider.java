package com.icoolkj.platform.infra.storage;

import com.icoolkj.platform.common.storage.FileStoragePolicyUtils;
import com.icoolkj.platform.common.storage.FileStorageSettingsProvider;

import java.util.List;

/** YAML-only fallback when system module provider is absent. */
public class YamlFileStorageSettingsProvider implements FileStorageSettingsProvider {

    private final StorageProperties properties;

    YamlFileStorageSettingsProvider(StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getDefaultProvider() {
        return properties.getProvider();
    }

    @Override
    public List<String> getEnabledProviders() {
        return splitCsv(properties.getEnabledProviders());
    }

    @Override
    public long getMaxFileSizeMb() {
        return properties.getMaxFileSizeMb();
    }

    @Override
    public List<String> getAllowedExtensions() {
        return splitCsv(properties.getAllowedExtensions());
    }

    @Override
    public List<String> getBlockedExtensions() {
        return splitCsv(properties.getBlockedExtensions());
    }

    static List<String> splitCsv(String raw) {
        return FileStoragePolicyUtils.splitCsv(raw);
    }
}

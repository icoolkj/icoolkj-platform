package com.icoolkj.platform.common.storage;

import java.util.List;

/** Resolved avatar upload policy from {@code sys_config}. */
public interface AvatarSettingsProvider {

    long getMaxSizeMb();

    List<String> getAllowedExtensions();

    int getOutputSize();

    int getThumbSmSize();

    int getThumbMdSize();

    boolean isContentScanEnabled();
}

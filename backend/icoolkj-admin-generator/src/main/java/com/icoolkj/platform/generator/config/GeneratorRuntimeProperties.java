package com.icoolkj.platform.generator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Runtime gates for the code generator module (api process). */
@ConfigurationProperties(prefix = "icoolkj.generator")
public class GeneratorRuntimeProperties {

    /** When false, generator REST controllers are not registered. */
    private boolean enabled = true;

    /** When false, {@code /tool/gen/genCode} disk writes are rejected (zip download still allowed). */
    private boolean diskWriteEnabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDiskWriteEnabled() {
        return diskWriteEnabled;
    }

    public void setDiskWriteEnabled(boolean diskWriteEnabled) {
        this.diskWriteEnabled = diskWriteEnabled;
    }
}

package com.icoolkj.platform.system.backup.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DatabaseBackupConfigUpdateRequest {

    private boolean enabled;

    @NotBlank
    @Size(max = 64)
    private String cron;

    @NotBlank
    @Size(max = 512)
    private String outputDir;

    @Min(0)
    @Max(3650)
    private int retentionDays;

    private boolean compress;
}

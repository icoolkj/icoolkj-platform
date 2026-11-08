package com.icoolkj.platform.system.job.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysJobUpdateRequest {

    @NotBlank(message = "{validation.id.required}")
    private String id;

    @NotBlank(message = "{validation.job.cron_required}")
    private String cronExpression;

    /** 0 normal / 1 pause */
    @NotBlank(message = "{validation.job.status_required}")
    private String status;

    /** 0 forbid concurrent / 1 allow */
    private String allowConcurrent;

    private String misfirePolicy;

    private String remark;
}

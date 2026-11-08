package com.icoolkj.platform.system.job.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysJobLogPageQuery extends PageQuery {

    private String jobCode;
    private String jobId;
    private String status;
    private String triggerType;
    private String triggerUserId;
}

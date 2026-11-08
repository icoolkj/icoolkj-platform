package com.icoolkj.platform.system.job.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysJobPageQuery extends PageQuery {

    private String jobCode;
    private String jobName;
    private String status;
    private String jobGroup;
}

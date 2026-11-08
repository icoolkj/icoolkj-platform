package com.icoolkj.platform.workflow.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 流程模型分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WorkflowModelPageQuery extends PageQuery {

    private String modelKey;
    private String modelName;
    private String status;
    /** 列表范围租户（平台=0，租户=当前租户） */
    private String scopeTenantId;
    /** 租户列表是否联查平台（tenant_id=0）预置模型 */
    private boolean includePlatform;
}

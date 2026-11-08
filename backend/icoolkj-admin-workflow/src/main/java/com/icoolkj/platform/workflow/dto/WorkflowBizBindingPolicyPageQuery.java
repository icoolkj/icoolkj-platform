package com.icoolkj.platform.workflow.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 业务绑定策略分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WorkflowBizBindingPolicyPageQuery extends PageQuery {

    private String bizType;
    private String bindingMode;
}

package com.icoolkj.platform.workflow.spi;

import com.icoolkj.platform.workflow.domain.WfModelEntity;
import lombok.Builder;
import lombok.Getter;

/** 流程启动上下文，供 {@link WorkflowStartPreparer} 注入变量 */
@Getter
@Builder
public class WorkflowStartContext {

    private final String tenantId;
    private final String bizType;
    private final String bizId;
    private final String bizTitle;
    private final String starterUserId;
    private final String starterUserName;
    private final WfModelEntity model;
}

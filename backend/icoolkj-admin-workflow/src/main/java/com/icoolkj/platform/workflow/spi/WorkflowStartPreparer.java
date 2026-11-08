package com.icoolkj.platform.workflow.spi;

import java.util.Collections;
import java.util.Map;

/**
 * 业务模块在流程启动前注入流程变量（审批人、业务上下文等）。
 * <p>
 * 新 bizType 接入：实现本接口并注册为 Spring Bean，无需修改 workflow 核心代码。
 */
public interface WorkflowStartPreparer {

    boolean supports(String bizType);

    Map<String, Object> prepareVariables(WorkflowStartContext context);

    default Map<String, Object> emptyVariables() {
        return Collections.emptyMap();
    }
}

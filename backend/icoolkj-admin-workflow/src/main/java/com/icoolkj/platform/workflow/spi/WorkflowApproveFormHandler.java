package com.icoolkj.platform.workflow.spi;

import java.util.Map;

/**
 * 审批完成时写回业务表单（仅节点配置为 W 的字段）。
 * 由业务域实现；工作流只做派发与字段权限过滤。
 */
public interface WorkflowApproveFormHandler {

    boolean supports(String bizType);

    /**
     * @param formData 已按节点 fieldPerms 过滤后的可写字段
     */
    void applyWritablePatch(String bizId,
                            String processInstanceId,
                            String taskDefinitionKey,
                            Map<String, Object> formData);
}

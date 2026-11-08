package com.icoolkj.platform.workflow.spi;

import java.util.Map;

/**
 * 任务完成前补充流程变量（如 OA 条件字段），避免网关表达式找不到标识符。
 */
public interface WorkflowRuntimeVariableEnricher {

    boolean supports(String bizType);

    /**
     * @return 需写入流程实例的变量；空 map 表示无需补充
     */
    Map<String, Object> enrich(String bizType, String bizId, String processInstanceId);
}

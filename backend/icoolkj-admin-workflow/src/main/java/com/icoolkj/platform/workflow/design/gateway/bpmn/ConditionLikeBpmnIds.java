package com.icoolkj.platform.workflow.design.gateway.bpmn;

import java.util.function.BiFunction;
import java.util.function.Function;

/** 条件类网关 BPMN 元素 id 策略（排他 / 包容共用发射逻辑，仅 id 前缀不同） */
public record ConditionLikeBpmnIds(
        Function<String, String> splitId,
        Function<String, String> mergeId,
        Function<String, String> defaultFlowId,
        BiFunction<String, String, String> branchFlowId,
        Function<String, String> mergeOutFlowId,
        String mergeTechnicalName) {
}

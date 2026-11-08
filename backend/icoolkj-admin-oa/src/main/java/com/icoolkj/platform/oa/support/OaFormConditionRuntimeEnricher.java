package com.icoolkj.platform.oa.support;

import com.icoolkj.platform.workflow.spi.WorkflowRuntimeVariableEnricher;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 审批完成前补齐条件变量，修复「设计器配了条件但未勾选 used_in_condition」导致的网关失败。 */
@Component
public class OaFormConditionRuntimeEnricher implements WorkflowRuntimeVariableEnricher {

    private final OaFormConditionVariableSupport conditionVariableSupport;

    public OaFormConditionRuntimeEnricher(OaFormConditionVariableSupport conditionVariableSupport) {
        this.conditionVariableSupport = conditionVariableSupport;
    }

    @Override
    public boolean supports(String bizType) {
        return StringUtils.hasText(bizType) && bizType.trim().toLowerCase(Locale.ROOT).startsWith("oa_");
    }

    @Override
    public Map<String, Object> enrich(String bizType, String bizId, String processInstanceId) {
        return conditionVariableSupport.resolveForRuntime(bizId, processInstanceId);
    }
}

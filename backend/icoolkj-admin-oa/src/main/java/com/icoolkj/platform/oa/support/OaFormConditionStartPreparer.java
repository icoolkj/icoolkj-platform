package com.icoolkj.platform.oa.support;

import com.icoolkj.platform.workflow.spi.WorkflowStartContext;
import com.icoolkj.platform.workflow.spi.WorkflowStartPreparer;
import java.util.Locale;
import java.util.Map;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * OA 系统表单：将条件字段注入启动变量（契约 27）。
 * <p>来源：{@code used_in_condition=1} ∪ 设计器条件表达式引用的 fieldCode。
 */
@Component
@Order(20)
public class OaFormConditionStartPreparer implements WorkflowStartPreparer {

    private final OaFormConditionVariableSupport conditionVariableSupport;

    public OaFormConditionStartPreparer(OaFormConditionVariableSupport conditionVariableSupport) {
        this.conditionVariableSupport = conditionVariableSupport;
    }

    @Override
    public boolean supports(String bizType) {
        return StringUtils.hasText(bizType) && bizType.trim().toLowerCase(Locale.ROOT).startsWith("oa_");
    }

    @Override
    public Map<String, Object> prepareVariables(WorkflowStartContext context) {
        if (context == null || !StringUtils.hasText(context.getBizId())) {
            return Map.of();
        }
        return conditionVariableSupport.resolveForStart(context.getBizId(), context.getModel());
    }
}

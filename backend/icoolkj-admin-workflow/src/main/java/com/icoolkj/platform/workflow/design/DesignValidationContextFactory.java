package com.icoolkj.platform.workflow.design;

import com.icoolkj.platform.workflow.support.WorkflowApproverRuleSupport;
import org.springframework.stereotype.Component;

/** 根级校验上下文工厂 */
@Component
public class DesignValidationContextFactory {

    private final WorkflowApproverRuleSupport approverRuleSupport;
    private final DesignNodeHandlerRegistry registry;

    public DesignValidationContextFactory(WorkflowApproverRuleSupport approverRuleSupport,
                                          DesignNodeHandlerRegistry registry) {
        this.approverRuleSupport = approverRuleSupport;
        this.registry = registry;
    }

    public DesignValidationContext root() {
        return new DesignValidationContext(approverRuleSupport, registry, false);
    }
}

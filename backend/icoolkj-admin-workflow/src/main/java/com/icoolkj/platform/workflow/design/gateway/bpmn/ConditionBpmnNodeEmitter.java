package com.icoolkj.platform.workflow.design.gateway.bpmn;

import com.icoolkj.platform.workflow.design.gateway.GatewayKind;
import org.springframework.stereotype.Component;

/** 排他网关 BPMN 发射 */
@Component
public class ConditionBpmnNodeEmitter extends AbstractConditionLikeBpmnNodeEmitter {

    public ConditionBpmnNodeEmitter() {
        super(GatewayKind.CONDITION);
    }
}

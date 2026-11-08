package com.icoolkj.platform.workflow.design.gateway.bpmn;

import com.icoolkj.platform.workflow.design.gateway.GatewayKind;
import org.springframework.stereotype.Component;

/** 包容网关 BPMN 发射 */
@Component
public class InclusiveBpmnNodeEmitter extends AbstractConditionLikeBpmnNodeEmitter {

    public InclusiveBpmnNodeEmitter() {
        super(GatewayKind.INCLUSIVE);
    }
}

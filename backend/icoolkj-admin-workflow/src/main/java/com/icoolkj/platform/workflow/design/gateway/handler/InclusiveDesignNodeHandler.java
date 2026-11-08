package com.icoolkj.platform.workflow.design.gateway.handler;

import com.icoolkj.platform.workflow.design.gateway.GatewayKind;
import org.springframework.stereotype.Component;

/** 包容网关（inclusive） */
@Component
public class InclusiveDesignNodeHandler extends AbstractConditionLikeDesignNodeHandler {

    public InclusiveDesignNodeHandler() {
        super(GatewayKind.INCLUSIVE);
    }
}

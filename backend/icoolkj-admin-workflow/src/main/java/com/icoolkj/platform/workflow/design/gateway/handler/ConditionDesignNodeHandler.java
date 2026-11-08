package com.icoolkj.platform.workflow.design.gateway.handler;

import com.icoolkj.platform.workflow.design.gateway.GatewayKind;
import org.springframework.stereotype.Component;

/** 排他网关（condition） */
@Component
public class ConditionDesignNodeHandler extends AbstractConditionLikeDesignNodeHandler {

    public ConditionDesignNodeHandler() {
        super(GatewayKind.CONDITION);
    }
}

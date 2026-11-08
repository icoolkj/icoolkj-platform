package com.icoolkj.platform.workflow.action;

import com.icoolkj.platform.workflow.constant.WfTaskActionTypes;
import org.springframework.stereotype.Component;

/** 委派动作类型注册（编排仍在 ProcessServiceImpl.delegateTask / resolveDelegateReturn）。 */
@Component
public class DelegateTaskActionHandler implements WorkflowTaskActionHandler {

    @Override
    public String actionType() {
        return WfTaskActionTypes.DELEGATE;
    }
}

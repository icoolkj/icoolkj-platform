package com.icoolkj.platform.workflow.action;

import com.icoolkj.platform.workflow.constant.WfTaskActionTypes;
import org.springframework.stereotype.Component;

/** 管理员自由跳转动作类型注册（编排仍在 ProcessServiceImpl.jumpTask）。 */
@Component
public class JumpTaskActionHandler implements WorkflowTaskActionHandler {

    @Override
    public String actionType() {
        return WfTaskActionTypes.JUMP;
    }
}

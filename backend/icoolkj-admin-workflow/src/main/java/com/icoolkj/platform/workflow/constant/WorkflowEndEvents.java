package com.icoolkj.platform.workflow.constant;

/** 平台 BPMN 结束节点 id 约定（各业务模板应保持一致，供引擎识别终态） */
public final class WorkflowEndEvents {

    public static final String APPROVED = "endApproved";
    public static final String REJECTED = "endRejected";

    private WorkflowEndEvents() {
    }
}

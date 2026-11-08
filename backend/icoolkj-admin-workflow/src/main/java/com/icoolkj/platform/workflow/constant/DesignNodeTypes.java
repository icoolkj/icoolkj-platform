package com.icoolkj.platform.workflow.constant;

/**
 * 设计节点类型与 BPMN category 唯一契约（与 design JSON {@code nodeType} 对齐）。
 * <p>Emitter / Handler / 抄送识别均引用本类，禁止各处再写 {@code "approve"} / {@code "cc"} 字面量。
 */
public final class DesignNodeTypes {

    public static final String APPROVE = "approve";
    public static final String CC = "cc";
    public static final String CONDITION = "condition";
    public static final String PARALLEL = "parallel";
    /** 包容网关（已实现） */
    public static final String INCLUSIVE = "inclusive";
    /** 延时等待（intermediateCatchEvent timer） */
    public static final String DELAY = "delay";
    /** 自动服务（serviceTask delegateExpression） */
    public static final String SERVICE = "service";
    /** 子流程（callActivity） */
    public static final String SUBPROCESS = "subprocess";

    public static final String BRANCH_YES = "yes";
    public static final String BRANCH_DEFAULT = "default";

    private DesignNodeTypes() {
    }
}

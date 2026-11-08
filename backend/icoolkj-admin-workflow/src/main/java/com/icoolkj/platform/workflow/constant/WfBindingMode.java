package com.icoolkj.platform.workflow.constant;

/** 业务类型与流程模型的绑定策略 */
public final class WfBindingMode {

    /** 强制使用平台流程（tenant_id=0），租户不可创建同 biz_type 模型 */
    public static final String PLATFORM_MANDATORY = "platform_mandatory";
    /** 租户模型优先，无则回落平台模板（兼容旧语义） */
    public static final String TENANT_PREFERRED = "tenant_preferred";
    /** 租户必配：提交仅用本租户已部署模型，不回落平台图 */
    public static final String TENANT_REQUIRED = "tenant_required";

    private WfBindingMode() {
    }
}

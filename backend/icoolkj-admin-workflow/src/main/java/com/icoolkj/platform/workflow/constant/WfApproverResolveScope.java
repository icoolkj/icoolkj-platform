package com.icoolkj.platform.workflow.constant;

/** 审批人用户查询范围 */
public final class WfApproverResolveScope {

    /** 在业务所属租户内解析用户/角色 */
    public static final String BIZ_TENANT = "biz_tenant";
    /** 在平台租户内解析（如平台管理员节点） */
    public static final String PLATFORM_TENANT = "platform_tenant";

    private WfApproverResolveScope() {
    }
}

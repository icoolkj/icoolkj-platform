package com.icoolkj.platform.system.notice.constant;

/** 公告审批流程角色编码（与 V2 种子数据一致） */
public final class NoticeWorkflowRoleCodes {

    /** 平台超级管理员 */
    public static final String PLATFORM_SUPER_ADMIN = "platform_super_admin";
    /** 部门审批人（默认租户） */
    public static final String DEPT_APPROVER = "dept_approver";
    /** 租户管理员（部门审批人缺失时回退） */
    public static final String TENANT_ADMIN = "tenant_admin";

    private NoticeWorkflowRoleCodes() {
    }
}

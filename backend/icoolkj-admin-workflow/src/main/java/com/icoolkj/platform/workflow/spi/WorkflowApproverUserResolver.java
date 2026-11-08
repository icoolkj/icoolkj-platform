package com.icoolkj.platform.workflow.spi;

import java.util.List;

/** 按角色/租户解析审批人用户 ID（业务模块实现，含跨租户平台用户查询） */
public interface WorkflowApproverUserResolver {

    String resolveFirstByRoleCodes(String lookupTenantId, List<String> roleCodes);

    /** 角色下全部活跃用户（会签/或签） */
    List<String> resolveAllByRoleCodes(String lookupTenantId, List<String> roleCodes);

    /** 发起人所在部门内，按角色优先级解析审批人 */
    String resolveStarterDeptRole(String lookupTenantId, String starterUserId, List<String> roleCodes);

    /** 发起人同部门同角色全部用户 */
    List<String> resolveAllStarterDeptRole(String lookupTenantId, String starterUserId, List<String> roleCodes);

    /** 表达式 starter：返回发起人 userId */
    String resolveStarterUserId(String starterUserId);
}

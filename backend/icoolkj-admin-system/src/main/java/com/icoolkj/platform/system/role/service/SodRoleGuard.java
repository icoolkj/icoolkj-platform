package com.icoolkj.platform.system.role.service;

import java.util.Collection;

/**
 * Enforces 三权角色互斥（同一用户不可同时持有两个及以上三权角色）.
 */
public interface SodRoleGuard {

    void assertAssignable(String tenantId, Collection<String> roleIds);
}

package com.icoolkj.platform.infra.async;

import org.springframework.security.core.Authentication;

/**
 * Rebuilds a full security principal for worker-side async execution.
 * Implemented in system (loads user + RBAC); infra falls back to a minimal LoginUser if absent.
 */
@FunctionalInterface
public interface AsyncTaskPrincipalResolver {

    /**
     * @return authentication for the task owner, or {@code null} if user cannot be resolved
     */
    Authentication resolve(String tenantId, String userId);
}

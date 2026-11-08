package com.icoolkj.platform.system.datascope;

import java.util.Collection;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** Shared L3 match rules for SQL handlers and {@link DataScopeRowAccessEvaluator}. */
public final class DataScopeMatchSupport {

    private DataScopeMatchSupport() {
    }

    public static boolean matchesSelf(boolean hasUserColumn,
                                      boolean hasDeptColumn,
                                      String ownerUserId,
                                      String ownerDeptId,
                                      String operatorUserId,
                                      String operatorDeptId) {
        if (hasUserColumn) {
            return StringUtils.hasText(ownerUserId) && ownerUserId.equals(operatorUserId);
        }
        if (hasDeptColumn && StringUtils.hasText(operatorDeptId)) {
            return StringUtils.hasText(ownerDeptId) && ownerDeptId.equals(operatorDeptId);
        }
        return false;
    }

    public static boolean matchesDept(boolean hasUserColumn,
                                      boolean hasDeptColumn,
                                      String ownerUserId,
                                      String ownerDeptId,
                                      String operatorUserId,
                                      String operatorDeptId) {
        if (hasDeptColumn && StringUtils.hasText(operatorDeptId)) {
            return StringUtils.hasText(ownerDeptId) && ownerDeptId.equals(operatorDeptId);
        }
        return matchesSelf(
                hasUserColumn, hasDeptColumn, ownerUserId, ownerDeptId, operatorUserId, operatorDeptId);
    }

    public static boolean matchesDeptAndChild(boolean hasUserColumn,
                                              boolean hasDeptColumn,
                                              String ownerUserId,
                                              String ownerDeptId,
                                              String ownerAncestors,
                                              String operatorUserId,
                                              String operatorDeptId) {
        if (hasDeptColumn && StringUtils.hasText(operatorDeptId)) {
            if (!StringUtils.hasText(ownerDeptId)) {
                return false;
            }
            if (ownerDeptId.equals(operatorDeptId)) {
                return true;
            }
            return ancestorsContain(ownerAncestors, operatorDeptId);
        }
        return matchesSelf(
                hasUserColumn, hasDeptColumn, ownerUserId, ownerDeptId, operatorUserId, operatorDeptId);
    }

    /** @return {@code null} → fall back to self */
    public static Boolean matchesCustomOrFallbackSelf(String ownerDeptId, Collection<String> allowedDeptIds) {
        if (!StringUtils.hasText(ownerDeptId)) {
            return Boolean.FALSE;
        }
        if (CollectionUtils.isEmpty(allowedDeptIds)) {
            return null;
        }
        for (String deptId : allowedDeptIds) {
            if (ownerDeptId.equals(deptId)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public static boolean ancestorsContain(String ancestors, String operatorDeptId) {
        if (!StringUtils.hasText(ancestors) || !StringUtils.hasText(operatorDeptId)) {
            return false;
        }
        for (String part : ancestors.split(",")) {
            if (operatorDeptId.equals(part.trim())) {
                return true;
            }
        }
        return false;
    }
}

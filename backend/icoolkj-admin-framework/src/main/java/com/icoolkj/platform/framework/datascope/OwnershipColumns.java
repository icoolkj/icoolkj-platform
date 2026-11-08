package com.icoolkj.platform.framework.datascope;

import com.icoolkj.platform.common.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.util.function.Consumer;

/** Fill {@code user_id} / {@code dept_id} ownership columns. */
public final class OwnershipColumns {

    private OwnershipColumns() {
    }

    public static void applyUser(Consumer<String> setUserId, String ownerUserId) {
        if (!StringUtils.hasText(ownerUserId)) {
            throw new BusinessException("error.datascope.owner_required");
        }
        setUserId.accept(ownerUserId.trim());
    }

    public static void applyUserAndDept(Consumer<String> setUserId,
                                        Consumer<String> setDeptId,
                                        String ownerUserId,
                                        String ownerDeptId) {
        applyUser(setUserId, ownerUserId);
        if (!StringUtils.hasText(ownerDeptId)) {
            throw new BusinessException("error.datascope.owner_required");
        }
        setDeptId.accept(ownerDeptId.trim());
    }

    public static void applyUserAndDept(Consumer<String> setUserId,
                                        Consumer<String> setDeptId,
                                        String ownerUserId,
                                        OwnerDeptLookup deptLookup) {
        applyUser(setUserId, ownerUserId);
        if (deptLookup == null) {
            throw new BusinessException("error.datascope.owner_required");
        }
        String deptId = deptLookup.findDeptIdByUserId(ownerUserId.trim());
        if (!StringUtils.hasText(deptId)) {
            throw new BusinessException("error.datascope.owner_required");
        }
        setDeptId.accept(deptId.trim());
    }
}

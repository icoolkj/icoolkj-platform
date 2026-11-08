package com.icoolkj.platform.framework.datascope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.icoolkj.platform.common.exception.BusinessException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class OwnershipColumnsTest {

    @Test
    void applyUserRequiresOwner() {
        AtomicReference<String> userId = new AtomicReference<>();
        assertThrows(BusinessException.class, () -> OwnershipColumns.applyUser(userId::set, null));
        assertThrows(BusinessException.class, () -> OwnershipColumns.applyUser(userId::set, "  "));
        OwnershipColumns.applyUser(userId::set, "u1");
        assertEquals("u1", userId.get());
    }

    @Test
    void applyUserAndDeptRequiresBoth() {
        AtomicReference<String> userId = new AtomicReference<>();
        AtomicReference<String> deptId = new AtomicReference<>();
        assertThrows(BusinessException.class,
                () -> OwnershipColumns.applyUserAndDept(userId::set, deptId::set, "u1", (String) null));
        OwnershipColumns.applyUserAndDept(userId::set, deptId::set, "u1", "d1");
        assertEquals("u1", userId.get());
        assertEquals("d1", deptId.get());
    }

    @Test
    void applyUserAndDeptRequiresLookupResult() {
        AtomicReference<String> userId = new AtomicReference<>();
        AtomicReference<String> deptId = new AtomicReference<>();
        assertThrows(BusinessException.class,
                () -> OwnershipColumns.applyUserAndDept(userId::set, deptId::set, "u1", (OwnerDeptLookup) null));
        assertThrows(BusinessException.class,
                () -> OwnershipColumns.applyUserAndDept(userId::set, deptId::set, "u1", id -> null));
        OwnershipColumns.applyUserAndDept(userId::set, deptId::set, "u1", id -> "d9");
        assertEquals("d9", deptId.get());
    }
}

package com.icoolkj.platform.system.role.service.impl;

import com.icoolkj.platform.common.constant.SodRoleCodes;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.system.role.domain.RoleEntity;
import com.icoolkj.platform.system.role.mapper.RoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SodRoleGuardImplTest {

    private static final String TENANT_ID = "tenant-1";

    @Mock
    private RoleMapper roleMapper;

    private SodRoleGuardImpl guard;

    @BeforeEach
    void setUp() {
        guard = new SodRoleGuardImpl(roleMapper);
    }

    @Test
    void rejectsMultipleSodRoles() {
        when(roleMapper.selectById("sys")).thenReturn(role(SodRoleCodes.SYS_ADMIN, TENANT_ID));
        when(roleMapper.selectById("audit")).thenReturn(role(SodRoleCodes.AUDIT_ADMIN, TENANT_ID));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.assertAssignable(TENANT_ID, List.of("sys", "audit")));

        assertEquals("error.user.sod_role_conflict", ex.getMessage());
    }

    @Test
    void acceptsSingleSodRoleAndOrdinaryRole() {
        when(roleMapper.selectById("sys")).thenReturn(role(SodRoleCodes.SYS_ADMIN, TENANT_ID));
        when(roleMapper.selectById("ordinary")).thenReturn(role("employee", TENANT_ID));

        assertDoesNotThrow(() -> guard.assertAssignable(TENANT_ID, List.of("sys", "ordinary")));
    }

    @Test
    void ignoresRolesFromAnotherTenant() {
        when(roleMapper.selectById("sys")).thenReturn(role(SodRoleCodes.SYS_ADMIN, TENANT_ID));
        when(roleMapper.selectById("audit")).thenReturn(role(SodRoleCodes.AUDIT_ADMIN, "tenant-2"));

        assertDoesNotThrow(() -> guard.assertAssignable(TENANT_ID, List.of("sys", "audit")));
    }

    private RoleEntity role(String roleCode, String tenantId) {
        RoleEntity role = new RoleEntity();
        role.setRoleCode(roleCode);
        role.setTenantId(tenantId);
        return role;
    }
}

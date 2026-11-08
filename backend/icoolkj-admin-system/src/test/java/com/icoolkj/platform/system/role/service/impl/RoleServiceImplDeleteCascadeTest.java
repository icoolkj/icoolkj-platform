package com.icoolkj.platform.system.role.service.impl;

import com.icoolkj.platform.framework.security.service.LoginSessionService;
import com.icoolkj.platform.system.role.domain.RoleDeptEntity;
import com.icoolkj.platform.system.role.domain.RoleEntity;
import com.icoolkj.platform.system.role.domain.RoleMenuEntity;
import com.icoolkj.platform.system.user.domain.UserRoleEntity;
import com.icoolkj.platform.system.role.mapper.RoleDeptMapper;
import com.icoolkj.platform.system.role.mapper.RoleMapper;
import com.icoolkj.platform.system.role.mapper.RoleMenuMapper;
import com.icoolkj.platform.system.user.mapper.UserRoleMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplDeleteCascadeTest {

    @Mock
    private RoleMapper roleMapper;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private RoleMenuMapper roleMenuMapper;
    @Mock
    private RoleDeptMapper roleDeptMapper;
    @Mock
    private LoginSessionService loginSessionService;

    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleServiceImpl(
                roleMapper, userRoleMapper, roleMenuMapper, roleDeptMapper, loginSessionService);
    }

    @Test
    void deleteByIdClearsLinksAndRevokesSessions() {
        RoleEntity role = new RoleEntity();
        role.setId("r1");
        role.setTenantId("t1");
        role.setDeleted(0);

        UserRoleEntity link = new UserRoleEntity();
        link.setUserId("u1");
        link.setRoleId("r1");
        link.setTenantId("t1");

        when(roleMapper.selectById("r1")).thenReturn(role);
        when(userRoleMapper.selectList(any(UserRoleEntity.class))).thenReturn(List.of(link));
        when(roleMapper.updateById(any(RoleEntity.class))).thenReturn(1);

        roleService.deleteById("r1");

        ArgumentCaptor<RoleEntity> softDelete = ArgumentCaptor.forClass(RoleEntity.class);
        verify(roleMapper).updateById(softDelete.capture());
        assertEquals(1, softDelete.getValue().getDeleted());

        ArgumentCaptor<UserRoleEntity> userRoleDelete = ArgumentCaptor.forClass(UserRoleEntity.class);
        verify(userRoleMapper).delete(userRoleDelete.capture());
        assertEquals("t1", userRoleDelete.getValue().getTenantId());
        assertEquals("r1", userRoleDelete.getValue().getRoleId());

        ArgumentCaptor<RoleMenuEntity> roleMenuDelete = ArgumentCaptor.forClass(RoleMenuEntity.class);
        verify(roleMenuMapper).delete(roleMenuDelete.capture());
        assertEquals("r1", roleMenuDelete.getValue().getRoleId());

        ArgumentCaptor<RoleDeptEntity> roleDeptDelete = ArgumentCaptor.forClass(RoleDeptEntity.class);
        verify(roleDeptMapper).delete(roleDeptDelete.capture());
        assertEquals("r1", roleDeptDelete.getValue().getRoleId());

        verify(loginSessionService).removeAllUserSessions(eq("t1"), eq("u1"));
    }
}

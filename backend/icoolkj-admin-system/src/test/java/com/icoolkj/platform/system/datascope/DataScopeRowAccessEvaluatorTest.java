package com.icoolkj.platform.system.datascope;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.icoolkj.platform.common.enums.DataScopeEnum;
import com.icoolkj.platform.common.enums.StatusEnum;
import com.icoolkj.platform.framework.datascope.DataScopeSpec;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.system.dept.domain.DeptEntity;
import com.icoolkj.platform.system.role.domain.RoleDeptEntity;
import com.icoolkj.platform.system.role.domain.RoleEntity;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.dept.mapper.DeptMapper;
import com.icoolkj.platform.system.role.mapper.RoleDeptMapper;
import com.icoolkj.platform.system.role.mapper.RoleMapper;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.user.mapper.UserRoleMapper;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class DataScopeRowAccessEvaluatorTest {

    private static final String TENANT = "t1";
    private static final String OP_USER = "u-op";
    private static final String OP_DEPT = "d-op";

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private RoleDeptMapper roleDeptMapper;
    @Mock
    private DeptMapper deptMapper;

    private DataScopeRowAccessEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new DataScopeRowAccessEvaluator(
                userMapper, userRoleMapper, roleMapper, roleDeptMapper, deptMapper);
        LoginUser login = new LoginUser();
        login.setUserId(OP_USER);
        login.setTenantId(TENANT);
        login.setUsername("op");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(login, null, login.getAuthorities()));

        UserEntity op = new UserEntity();
        op.setId(OP_USER);
        op.setDeptId(OP_DEPT);
        op.setTenantId(TENANT);
        when(userMapper.selectById(OP_USER)).thenReturn(op);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allRoleShortCircuitsAllow() {
        when(userRoleMapper.selectRoleIdsByTenantAndUser(TENANT, OP_USER)).thenReturn(List.of("r-all"));
        when(roleMapper.selectById("r-all")).thenReturn(role(DataScopeEnum.ALL));

        assertTrue(evaluator.isReadable(DataScopeSpec.userAndDept(), "other", "other-dept"));
    }

    @Test
    void selfOnlySeesOwnUser() {
        when(userRoleMapper.selectRoleIdsByTenantAndUser(TENANT, OP_USER)).thenReturn(List.of("r-self"));
        when(roleMapper.selectById("r-self")).thenReturn(role(DataScopeEnum.SELF));

        assertTrue(evaluator.isReadable(DataScopeSpec.userAndDept(), OP_USER, OP_DEPT));
        assertFalse(evaluator.isReadable(DataScopeSpec.userAndDept(), "other", OP_DEPT));
    }

    @Test
    void deptScopeDeniesEmptyOwnerDeptEvenIfSelfUser() {
        when(userRoleMapper.selectRoleIdsByTenantAndUser(TENANT, OP_USER)).thenReturn(List.of("r-dept"));
        when(roleMapper.selectById("r-dept")).thenReturn(role(DataScopeEnum.DEPT));

        assertFalse(evaluator.isReadable(DataScopeSpec.userAndDept(), OP_USER, null));
        assertTrue(evaluator.isReadable(DataScopeSpec.userAndDept(), "other", OP_DEPT));
    }

    @Test
    void deptAndChildMatchesAncestorPath() {
        when(userRoleMapper.selectRoleIdsByTenantAndUser(TENANT, OP_USER)).thenReturn(List.of("r-tree"));
        when(roleMapper.selectById("r-tree")).thenReturn(role(DataScopeEnum.DEPT_AND_CHILD));

        DeptEntity child = new DeptEntity();
        child.setId("d-child");
        child.setTenantId(TENANT);
        child.setAncestors("0," + OP_DEPT);
        when(deptMapper.selectById("d-child")).thenReturn(child);

        assertTrue(evaluator.isReadable(DataScopeSpec.userAndDept(), "other", "d-child"));
        assertFalse(evaluator.isReadable(DataScopeSpec.userAndDept(), "other", null));
    }

    @Test
    void customFallsBackToSelfWhenNoRoleDepts() {
        when(userRoleMapper.selectRoleIdsByTenantAndUser(TENANT, OP_USER)).thenReturn(List.of("r-custom"));
        when(roleMapper.selectById("r-custom")).thenReturn(role(DataScopeEnum.CUSTOM));
        when(roleDeptMapper.selectList(any(RoleDeptEntity.class))).thenReturn(List.of());

        assertTrue(evaluator.isReadable(DataScopeSpec.userAndDept(), OP_USER, OP_DEPT));
        assertFalse(evaluator.isReadable(DataScopeSpec.userAndDept(), "other", OP_DEPT));
    }

    @Test
    void orMergeSelfAndDeptWidens() {
        when(userRoleMapper.selectRoleIdsByTenantAndUser(TENANT, OP_USER))
                .thenReturn(List.of("r-self", "r-dept"));
        when(roleMapper.selectById("r-self")).thenReturn(role(DataScopeEnum.SELF));
        when(roleMapper.selectById("r-dept")).thenReturn(role(DataScopeEnum.DEPT));

        assertTrue(evaluator.isReadable(DataScopeSpec.userAndDept(), "peer", OP_DEPT));
        assertTrue(evaluator.isReadable(DataScopeSpec.userAndDept(), OP_USER, "other-dept"));
    }

    private static RoleEntity role(DataScopeEnum scope) {
        RoleEntity role = new RoleEntity();
        role.setStatus(StatusEnum.NORMAL.getCode());
        role.setDataScope(scope.getCode());
        return role;
    }
}

package com.icoolkj.platform.system.user.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.datascope.RowAccessSupport;
import com.icoolkj.platform.framework.excel.ExcelImportLookup;
import com.icoolkj.platform.framework.excel.ExcelImportResult;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.framework.security.crypto.PasswordTransportCipher;
import com.icoolkj.platform.framework.security.service.LoginSessionService;
import com.icoolkj.platform.framework.tenant.TenantContext;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.system.dept.domain.DeptEntity;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.user.excel.UserImportRow;
import com.icoolkj.platform.system.dept.mapper.DeptMapper;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.auth.service.LoginSecurityPolicyService;
import com.icoolkj.platform.system.user.service.PasswordHistoryService;
import com.icoolkj.platform.system.user.service.PasswordPolicyService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.icoolkj.platform.system.auth.service.impl.LoginSecurityConfigService;

@ExtendWith(MockitoExtension.class)
class UserServiceImplImportRowsTest {

    private static final String TENANT = "tenant-biz-1";

    @Mock private UserMapper userMapper;
    @Mock private DeptMapper deptMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PasswordPolicyService passwordPolicyService;
    @Mock private PasswordHistoryService passwordHistoryService;
    @Mock private LoginSecurityConfigService loginSecurityConfigService;
    @Mock private LoginSessionService loginSessionService;
    @Mock private LoginSecurityPolicyService loginSecurityPolicyService;
    @Mock private RowAccessSupport rowAccessSupport;
    @Mock private Validator validator;
    @Mock private I18nMessageResolver i18n;
    @Mock private ExcelImportLookup excelImportLookup;
    @Mock private PasswordTransportCipher passwordTransportCipher;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userMapper,
                deptMapper,
                passwordEncoder,
                passwordPolicyService,
                passwordHistoryService,
                loginSecurityConfigService,
                loginSessionService,
                loginSecurityPolicyService,
                rowAccessSupport,
                validator,
                i18n,
                excelImportLookup,
                passwordTransportCipher);
        TenantContextHolder.set(TenantContext.tenant(TENANT, "biz"));
        lenient().when(i18n.resolve(anyString(), any(Object[].class)))
                .thenAnswer(inv -> inv.getArgument(0));
        lenient().when(validator.validate(any())).thenReturn(Collections.emptySet());
        lenient().when(passwordTransportCipher.unwrapIfEncrypted(anyString()))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void emptyRowsOk() {
        ExcelImportResult result = userService.importRows(List.of());
        assertEquals(0, result.success());
        verify(userMapper, never()).insert(any());
    }

    @Test
    void blankRowsSkipped() {
        UserImportRow blank = new UserImportRow();
        ExcelImportResult result = userService.importRows(List.of(blank));
        assertEquals(0, result.success());
        assertEquals(1, result.skipped());
        verify(userMapper, never()).insert(any());
    }

    @Test
    void duplicateUsernameInFileRejectedWithoutPersist() {
        UserImportRow a = validRow("alice");
        UserImportRow b = validRow("Alice");
        stubResolveOk();

        ExcelImportResult result = userService.importRows(List.of(a, b));

        assertEquals(0, result.success());
        assertTrue(result.errorTotal() >= 1);
        verify(userMapper, never()).insert(any());
    }

    @Test
    void deptResolveFailureRejectedWithoutPersist() {
        UserImportRow row = validRow("bob");
        when(excelImportLookup.resolveDeptId("研发")).thenThrow(
                new BusinessException("error.excel.import_dept_not_found", "研发"));

        ExcelImportResult result = userService.importRows(List.of(row));

        assertEquals(0, result.success());
        assertEquals(1, result.errorTotal());
        verify(userMapper, never()).insert(any());
    }

    @Test
    void existingUsernameRejectedWithoutPersist() {
        UserImportRow row = validRow("carol");
        stubResolveOk();
        UserEntity existing = new UserEntity();
        existing.setId("u-existing");
        when(userMapper.selectByTenantAndUsername(TENANT, "carol")).thenReturn(existing);

        ExcelImportResult result = userService.importRows(List.of(row));

        assertEquals(0, result.success());
        assertEquals(1, result.errorTotal());
        verify(userMapper, never()).insert(any());
    }

    @Test
    void happyPathPersistsViaCreate() {
        UserImportRow row = validRow("dave");
        stubResolveOk();
        stubCreateDeps();

        ExcelImportResult result = userService.importRows(List.of(row));

        assertEquals(1, result.success());
        assertEquals(0, result.errorTotal());
        verify(userMapper, times(1)).insert(any(UserEntity.class));
    }

    @Test
    void midPersistFailureMarksRollbackOnly() {
        UserImportRow a = validRow("erin");
        UserImportRow b = validRow("frank");
        stubResolveOk();
        stubCreateDeps();
        // Validation phase passes; second create fails after first insert
        org.mockito.Mockito.doNothing()
                .doThrow(new BusinessException("error.user.username_exists"))
                .when(passwordPolicyService)
                .validate(eq(TENANT), anyString());

        TransactionStatus tx = mock(TransactionStatus.class);
        try (var mocked = mockStatic(TransactionAspectSupport.class)) {
            mocked.when(TransactionAspectSupport::currentTransactionStatus).thenReturn(tx);

            ExcelImportResult result = userService.importRows(List.of(a, b));

            assertEquals(0, result.success());
            assertTrue(result.errorTotal() >= 1);
            verify(tx).setRollbackOnly();
            verify(userMapper, times(1)).insert(any(UserEntity.class));
        }
    }

    private void stubResolveOk() {
        when(excelImportLookup.resolveDeptId("研发")).thenReturn("dept-1");
        when(excelImportLookup.resolveDictCode(eq("sys_gender"), anyString())).thenReturn("0");
        when(excelImportLookup.resolveDictCode(eq("sys_common_status"), anyString())).thenReturn("0");
    }

    private void stubCreateDeps() {
        when(userMapper.selectByTenantAndUsername(eq(TENANT), anyString())).thenReturn(null);
        DeptEntity dept = new DeptEntity();
        dept.setId("dept-1");
        dept.setTenantId(TENANT);
        dept.setStatus("0");
        when(deptMapper.selectById("dept-1")).thenReturn(dept);
        doNothing().when(rowAccessSupport).assertDataScope(any(), isNull(), eq("dept-1"));
        when(loginSecurityConfigService.requireInitialPassword(TENANT)).thenReturn("StudyOnly@2026");
        lenient().doNothing().when(passwordPolicyService).validate(eq(TENANT), anyString());
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("hash");
        lenient().when(loginSecurityConfigService.isForceChangeInitial(TENANT)).thenReturn(false);
        lenient().doNothing().when(passwordHistoryService).record(eq(TENANT), anyString(), anyString());
    }

    private static UserImportRow validRow(String username) {
        UserImportRow row = new UserImportRow();
        row.setUsername(username);
        row.setNickname(username);
        row.setDeptName("研发");
        row.setGender("男");
        row.setStatus("正常");
        return row;
    }
}

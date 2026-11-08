package com.icoolkj.platform.infra.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.infra.domain.InfraFileAccessPolicyEntity;
import com.icoolkj.platform.infra.dto.InfraFileAccessPolicyUpsertRequest;
import com.icoolkj.platform.infra.fileaccess.FileAccessPolicyRegistry;
import com.icoolkj.platform.infra.mapper.InfraFileAccessPolicyMapper;
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
class InfraFileAccessPolicyAdminServiceImplPinnedTest {

    @Mock
    private InfraFileAccessPolicyMapper policyMapper;
    @Mock
    private FileAccessPolicyRegistry policyRegistry;

    private InfraFileAccessPolicyAdminServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InfraFileAccessPolicyAdminServiceImpl(policyMapper, policyRegistry);
        LoginUser login = new LoginUser();
        login.setUserId("ops");
        login.setTenantId(TenantConstants.PLATFORM_TENANT_ID);
        login.setUsername("ops");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(login, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void upsert_rejectsPinnedExcelPattern() {
        InfraFileAccessPolicyUpsertRequest req = new InfraFileAccessPolicyUpsertRequest();
        req.setBizTypePattern("excel-export");
        req.setAuthMode("SESSION");
        req.setTenantScope("OWN");
        req.setVisibility("ALWAYS");
        assertThrows(BusinessException.class, () -> service.upsert(req));
        verify(policyMapper, never()).insert(any());
        verify(policyMapper, never()).updateById(any());
    }

    @Test
    void delete_rejectsPinnedOaInstance() {
        InfraFileAccessPolicyEntity row = new InfraFileAccessPolicyEntity();
        row.setId("p1");
        row.setBizTypePattern("oa_instance");
        when(policyMapper.selectById("p1")).thenReturn(row);
        assertThrows(BusinessException.class, () -> service.delete("p1"));
        verify(policyMapper, never()).logicalDeleteById(any(), any(), any());
    }
}

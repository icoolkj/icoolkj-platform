package com.icoolkj.platform.system.user.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import com.icoolkj.platform.system.auth.service.impl.LoginSecurityConfigService;

@ExtendWith(MockitoExtension.class)
class PasswordPolicyServiceImplTest {

    private static final String TENANT = "t1";

    @Mock
    private LoginSecurityConfigService loginSecurityConfigService;

    private PasswordPolicyServiceImpl passwordPolicyService;

    @BeforeEach
    void setUp() {
        passwordPolicyService = new PasswordPolicyServiceImpl(loginSecurityConfigService);
        lenient().when(loginSecurityConfigService.getPasswordMinLength(eq(TENANT), anyInt())).thenReturn(10);
        lenient().when(loginSecurityConfigService.isPasswordRequireUpper(TENANT)).thenReturn(true);
        lenient().when(loginSecurityConfigService.isPasswordRequireLower(TENANT)).thenReturn(true);
        lenient().when(loginSecurityConfigService.isPasswordRequireNumber(TENANT)).thenReturn(true);
        lenient().when(loginSecurityConfigService.isPasswordRequireSpecial(TENANT)).thenReturn(true);
    }

    @Test
    void acceptsPasswordMeetingPolicy() {
        assertDoesNotThrow(() -> passwordPolicyService.validate(TENANT, "StudyOnly@2026"));
    }

    @Test
    void rejectsMissingUppercase() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> passwordPolicyService.validate(TENANT, "admin@123456"));
        assertEquals("error.user.password_require_upper", ex.getMessage());
    }

    @Test
    void rejectsMissingLowercase() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> passwordPolicyService.validate(TENANT, "ADMIN@123456"));
        assertEquals("error.user.password_require_lower", ex.getMessage());
    }

    @Test
    void rejectsMissingDigit() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> passwordPolicyService.validate(TENANT, "Admin@Passwd"));
        assertEquals("error.user.password_require_number", ex.getMessage());
    }

    @Test
    void rejectsMissingSpecial() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> passwordPolicyService.validate(TENANT, "Admin12345"));
        assertEquals("error.user.password_require_special", ex.getMessage());
    }

    @Test
    void rejectsTooShort() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> passwordPolicyService.validate(TENANT, "Ad@1"));
        assertEquals("error.user.password_too_short", ex.getMessage());
    }
}

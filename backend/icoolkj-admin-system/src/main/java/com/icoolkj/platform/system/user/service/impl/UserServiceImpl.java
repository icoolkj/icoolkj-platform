package com.icoolkj.platform.system.user.service.impl;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.enums.StatusEnum;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.datascope.DataScopeSpec;
import com.icoolkj.platform.framework.datascope.RowAccessSupport;
import com.icoolkj.platform.framework.excel.ExcelImportI18n;
import com.icoolkj.platform.framework.excel.ExcelImportLookup;
import com.icoolkj.platform.framework.excel.ExcelImportResult;
import com.icoolkj.platform.framework.excel.ExcelImportRowError;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.system.user.convert.UserConvert;
import com.icoolkj.platform.system.dept.domain.DeptEntity;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.user.dto.UserCreateRequest;
import com.icoolkj.platform.system.user.dto.UserPageQuery;
import com.icoolkj.platform.system.user.dto.UserResetPasswordRequest;
import com.icoolkj.platform.system.user.dto.UserUpdateRequest;
import com.icoolkj.platform.system.user.excel.UserImportRow;
import com.icoolkj.platform.system.dept.mapper.DeptMapper;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.auth.service.LoginSecurityPolicyService;
import com.icoolkj.platform.system.user.service.PasswordHistoryService;
import com.icoolkj.platform.system.user.service.PasswordPolicyService;
import com.icoolkj.platform.system.user.service.UserService;
import com.icoolkj.platform.system.user.vo.UserVO;
import com.icoolkj.platform.framework.security.crypto.PasswordTransportCipher;
import com.icoolkj.platform.framework.security.service.LoginSessionService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.icoolkj.platform.system.auth.service.impl.LoginSecurityConfigService;

/** 用户表服务实现 */
@Service
public class UserServiceImpl implements UserService {

    private static final String USER_TYPE_SYSTEM = "system";

    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;
    private final PasswordHistoryService passwordHistoryService;
    private final LoginSecurityConfigService loginSecurityConfigService;
    private final LoginSessionService loginSessionService;
    private final LoginSecurityPolicyService loginSecurityPolicyService;
    private final RowAccessSupport rowAccessSupport;
    private final Validator validator;
    private final I18nMessageResolver i18n;
    private final ExcelImportLookup excelImportLookup;
    private final PasswordTransportCipher passwordTransportCipher;

    public UserServiceImpl(UserMapper userMapper,
                           DeptMapper deptMapper,
                           PasswordEncoder passwordEncoder,
                           PasswordPolicyService passwordPolicyService,
                           PasswordHistoryService passwordHistoryService,
                           LoginSecurityConfigService loginSecurityConfigService,
                           LoginSessionService loginSessionService,
                           LoginSecurityPolicyService loginSecurityPolicyService,
                           RowAccessSupport rowAccessSupport,
                           Validator validator,
                           I18nMessageResolver i18n,
                           ExcelImportLookup excelImportLookup,
                           PasswordTransportCipher passwordTransportCipher) {
        this.userMapper = userMapper;
        this.deptMapper = deptMapper;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyService = passwordPolicyService;
        this.passwordHistoryService = passwordHistoryService;
        this.loginSecurityConfigService = loginSecurityConfigService;
        this.loginSessionService = loginSessionService;
        this.loginSecurityPolicyService = loginSecurityPolicyService;
        this.rowAccessSupport = rowAccessSupport;
        this.validator = validator;
        this.i18n = i18n;
        this.excelImportLookup = excelImportLookup;
        this.passwordTransportCipher = passwordTransportCipher;
    }

    @Override
    public PageResult<UserVO> page(UserPageQuery query) {
        return TenantQueryScope.runForExplicitTenant(query, () -> {
            long total = userMapper.countByQuery(query);
            if (total == 0) {
                return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
            }
            List<UserEntity> list = userMapper.selectPage(query);
            if (CollectionUtils.isEmpty(list)) {
                return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
            }
            return PageResult.of(list.stream().map(UserConvert::toVo).toList(), total, query.getPageNo(), query.getPageSize());
        });
    }

    @Override
    public UserVO getById(String id) {
        UserEntity entity = requireUser(id);
        assertRowAccess(entity);
        return UserConvert.toVo(entity);
    }

    @Override
    public String create(UserCreateRequest request) {
        String tenantId = requireBusinessTenantId();
        assertUsernameAvailable(tenantId, request.getUsername(), null);
        String rawPassword = resolvePasswordOrInitial(tenantId, request.getPassword());
        passwordPolicyService.validate(tenantId, rawPassword);
        assertDeptAssignable(tenantId, request.getDeptId());

        UserEntity entity = UserConvert.toEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        entity.setTenantId(tenantId);
        applyTenantUserDefaults(entity, tenantId, rawPassword);
        userMapper.insert(entity);
        passwordHistoryService.record(tenantId, entity.getId(), entity.getPasswordHash());
        return String.valueOf(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportResult importRows(List<UserImportRow> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return ExcelImportResult.ok(0, 0);
        }
        String tenantId = requireBusinessTenantId();
        List<ExcelImportRowError> errors = new ArrayList<>();
        List<AcceptedImport> accepted = new ArrayList<>();
        Set<String> usernamesInFile = new HashSet<>();
        int skipped = 0;
        for (int i = 0; i < rows.size(); i++) {
            int excelRow = i + 2;
            UserImportRow row = rows.get(i);
            if (isBlankImportRow(row)) {
                skipped++;
                continue;
            }
            UserCreateRequest request;
            try {
                request = toCreateRequest(row);
            } catch (BusinessException ex) {
                errors.add(new ExcelImportRowError(excelRow, ExcelImportI18n.encode(ex)));
                continue;
            }
            String error = validateImportRequest(request, tenantId, usernamesInFile);
            if (error != null) {
                errors.add(new ExcelImportRowError(excelRow, error));
                continue;
            }
            usernamesInFile.add(request.getUsername().trim().toLowerCase(Locale.ROOT));
            accepted.add(new AcceptedImport(excelRow, request));
        }
        if (!errors.isEmpty()) {
            return ExcelImportResult.rejected(rows.size(), skipped, errors);
        }
        for (AcceptedImport item : accepted) {
            try {
                create(item.request());
            } catch (BusinessException ex) {
                errors.add(new ExcelImportRowError(item.excelRow(), ExcelImportI18n.encode(ex)));
            }
        }
        if (!errors.isEmpty()) {
            org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus()
                    .setRollbackOnly();
            return ExcelImportResult.rejected(rows.size(), skipped, errors);
        }
        return ExcelImportResult.ok(accepted.size(), skipped);
    }

    private record AcceptedImport(int excelRow, UserCreateRequest request) {
    }

    private String validateImportRequest(UserCreateRequest request,
                                         String tenantId,
                                         Set<String> usernamesInFile) {
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            return ExcelImportI18n.encodeViolation(violations.iterator().next());
        }
        String usernameKey = request.getUsername().trim().toLowerCase(Locale.ROOT);
        if (usernamesInFile.contains(usernameKey)) {
            return ExcelImportI18n.encode("error.excel.import_duplicate_in_file", "username");
        }
        try {
            assertUsernameAvailable(tenantId, request.getUsername(), null);
            assertDeptAssignable(tenantId, request.getDeptId());
        } catch (BusinessException ex) {
            return ExcelImportI18n.encode(ex);
        }
        return null;
    }

    @Override
    public void update(UserUpdateRequest request) {
        UserEntity entity = requireUser(request.getId());
        assertNotPlatformSuperAdmin(entity, "error.user.super_admin_immutable");
        assertSameTenant(entity);
        assertRowAccess(entity);

        String tenantId = entity.getTenantId();
        assertUsernameAvailable(tenantId, request.getUsername(), entity.getId());
        assertDeptAssignable(tenantId, request.getDeptId());

        UserConvert.copyEditable(request, entity);
        userMapper.updateById(entity);
    }

    @Override
    public void deleteById(String id) {
        UserEntity entity = requireUser(id);
        assertNotPlatformSuperAdmin(entity, "error.user.super_admin_delete_forbidden");
        assertSameTenant(entity);
        assertRowAccess(entity);
        AuditFieldHelper.markDeleted(entity);
        userMapper.updateById(entity);
    }

    @Override
    public void resetPassword(String id, UserResetPasswordRequest request) {
        UserEntity entity = requireUser(id);
        assertNotPlatformSuperAdmin(entity, "error.user.super_admin_immutable");
        assertSameTenant(entity);
        assertRowAccess(entity);
        String tenantId = entity.getTenantId();
        String rawPassword = resolvePasswordOrInitial(
                tenantId, request == null ? null : request.getNewPassword());
        passwordPolicyService.validate(tenantId, rawPassword);
        String previousHash = entity.getPasswordHash();
        entity.setPasswordHash(passwordEncoder.encode(rawPassword));
        entity.setPwdUpdatedAt(LocalDateTime.now());
        entity.setMustChangePassword(
                loginSecurityConfigService.isForceChangeInitial(tenantId) ? CommonConstants.YES : CommonConstants.NO);
        entity.setLoginFailCount(0);
        entity.setLockedUntil(null);
        userMapper.updateById(entity);
        passwordHistoryService.record(tenantId, entity.getId(), previousHash);
        loginSessionService.removeAllUserSessions(tenantId, entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlock(String id) {
        UserEntity entity = requireUser(id);
        assertNotPlatformSuperAdmin(entity, "error.user.super_admin_immutable");
        assertSameTenant(entity);
        assertRowAccess(entity);
        userMapper.updateLockState(entity.getId(), 0, null);
        loginSecurityPolicyService.clearLoginFailCounts(entity.getTenantId(), entity.getUsername());
        loginSessionService.removeAllUserSessions(entity.getTenantId(), entity.getId());
    }

    private String resolvePasswordOrInitial(String tenantId, String rawPassword) {
        if (StringUtils.hasText(rawPassword)) {
            return passwordTransportCipher.unwrapIfEncrypted(rawPassword.trim());
        }
        return loginSecurityConfigService.requireInitialPassword(tenantId);
    }

    private UserEntity requireUser(String id) {
        UserEntity entity = userMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.user");
        }
        return entity;
    }

    /** 平台租户用户仅允许 Flyway 初始化，禁止通过管理接口创建。 */
    private String requireBusinessTenantId() {
        String tenantId = TenantContextHolder.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            throw new BusinessException("error.tenant.unresolved");
        }
        if (TenantConstants.PLATFORM_TENANT_ID.equals(tenantId)) {
            throw new BusinessException("error.user.platform_create_forbidden");
        }
        return tenantId;
    }

    private void assertSameTenant(UserEntity entity) {
        String currentTenantId = TenantContextHolder.getTenantId();
        if (!StringUtils.hasText(currentTenantId) || !currentTenantId.equals(entity.getTenantId())) {
            throw new BusinessException("error.entity.not_found", "entity.name.user");
        }
    }

    private void assertRowAccess(UserEntity entity) {
        rowAccessSupport.assertReadable(
                entity.getTenantId(), DataScopeSpec.userAsId(), entity.getId(), entity.getDeptId());
    }

    private void assertNotPlatformSuperAdmin(UserEntity entity, String messageKey) {
        if (YesNoEnum.isYes(entity.getIsSuperAdmin())) {
            throw new BusinessException(messageKey);
        }
    }

    private void assertUsernameAvailable(String tenantId, String username, String excludeUserId) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        UserEntity existing = userMapper.selectByTenantAndUsername(tenantId, username.trim());
        if (existing != null && !existing.getId().equals(excludeUserId)) {
            throw new BusinessException("error.user.username_exists");
        }
    }

    private void assertDeptAssignable(String tenantId, String deptId) {
        if (!StringUtils.hasText(deptId)) {
            throw new BusinessException("error.user.dept_required");
        }
        DeptEntity dept = deptMapper.selectById(deptId.trim());
        if (dept == null) {
            throw new BusinessException("error.user.dept_not_found");
        }
        if (!tenantId.equals(dept.getTenantId())) {
            throw new BusinessException("error.user.dept_tenant_mismatch");
        }
        if (!StatusEnum.isNormal(dept.getStatus())) {
            throw new BusinessException("error.user.dept_disabled");
        }
        // L3: operator must be allowed to place users into this department
        rowAccessSupport.assertDataScope(DataScopeSpec.deptAsId(), null, dept.getId());
    }

    private void applyTenantUserDefaults(UserEntity entity, String tenantId, String rawPassword) {
        entity.setUserType(USER_TYPE_SYSTEM);
        entity.setIsSuperAdmin(CommonConstants.NO);
        entity.setStatus(StringUtils.hasText(entity.getStatus()) ? entity.getStatus() : CommonConstants.STATUS_NORMAL);
        entity.setLoginFailCount(0);
        entity.setLockedUntil(null);
        entity.setLastLoginIp(null);
        entity.setLastLoginAt(null);
        entity.setPasswordHash(passwordEncoder.encode(rawPassword));
        entity.setPwdUpdatedAt(LocalDateTime.now());
        entity.setMustChangePassword(
                loginSecurityConfigService.isForceChangeInitial(tenantId) ? CommonConstants.YES : CommonConstants.NO
        );
        entity.setMfaEnabled(CommonConstants.NO);
        entity.setMfaSecretEnc(null);
        entity.setMfaBoundAt(null);
    }

    private static boolean isBlankImportRow(UserImportRow row) {
        return !StringUtils.hasText(row.getUsername())
                && !StringUtils.hasText(row.getNickname())
                && !StringUtils.hasText(row.getDeptName());
    }

    private UserCreateRequest toCreateRequest(UserImportRow row) {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername(trimToNull(row.getUsername()));
        request.setNickname(trimToNull(row.getNickname()));
        request.setRealName(trimToNull(row.getRealName()));
        request.setDeptId(excelImportLookup.resolveDeptId(trimToNull(row.getDeptName())));
        request.setPhone(trimToNull(row.getPhone()));
        request.setEmail(trimToNull(row.getEmail()));
        String genderRaw = trimToNull(row.getGender());
        if (genderRaw != null) {
            request.setGender(excelImportLookup.resolveDictCode("sys_gender", genderRaw));
        } else {
            request.setGender(CommonConstants.GENDER_UNKNOWN);
        }
        String statusRaw = trimToNull(row.getStatus());
        if (statusRaw != null) {
            request.setStatus(excelImportLookup.resolveDictCode("sys_common_status", statusRaw));
        } else {
            request.setStatus(CommonConstants.STATUS_NORMAL);
        }
        return request;
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}

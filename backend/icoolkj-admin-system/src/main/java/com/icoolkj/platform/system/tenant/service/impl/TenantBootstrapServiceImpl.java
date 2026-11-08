package com.icoolkj.platform.system.tenant.service.impl;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.TenantType;
import com.icoolkj.platform.common.enums.DataScopeEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.system.dept.domain.DeptEntity;
import com.icoolkj.platform.system.post.domain.PostEntity;
import com.icoolkj.platform.system.role.domain.RoleEntity;
import com.icoolkj.platform.system.tenant.domain.TenantEntity;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.user.domain.UserPostEntity;
import com.icoolkj.platform.system.user.domain.UserRoleEntity;
import com.icoolkj.platform.system.tenant.dto.TenantBootstrapOptions;
import com.icoolkj.platform.system.dept.mapper.DeptMapper;
import com.icoolkj.platform.system.post.mapper.PostMapper;
import com.icoolkj.platform.system.role.mapper.RoleMapper;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.user.mapper.UserPostMapper;
import com.icoolkj.platform.system.user.mapper.UserRoleMapper;
import com.icoolkj.platform.system.user.service.PasswordHistoryService;
import com.icoolkj.platform.system.user.service.PasswordPolicyService;
import com.icoolkj.platform.system.tenant.service.TenantBootstrapService;
import com.icoolkj.platform.system.tenant.vo.TenantProvisionVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.regex.Pattern;
import com.icoolkj.platform.system.auth.service.impl.LoginSecurityConfigService;

@Service
public class TenantBootstrapServiceImpl implements TenantBootstrapService {

    private static final String DEFAULT_DEPT_CODE = "root";
    private static final String USER_TYPE_SYSTEM = "system";

    private static final String CODE_TENANT_ADMIN = "tenant_admin";
    private static final String CODE_ORG_ADMIN = "org_admin";

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{2,49}$");
    private static final Pattern DEPT_CODE_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{0,49}$");

    private final DeptMapper deptMapper;
    private final PostMapper postMapper;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserPostMapper userPostMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginSecurityConfigService loginSecurityConfigService;
    private final PasswordPolicyService passwordPolicyService;
    private final PasswordHistoryService passwordHistoryService;
    private final I18nMessageResolver i18nMessageResolver;

    public TenantBootstrapServiceImpl(DeptMapper deptMapper,
                                      PostMapper postMapper,
                                      RoleMapper roleMapper,
                                      UserMapper userMapper,
                                      UserRoleMapper userRoleMapper,
                                      UserPostMapper userPostMapper,
                                      PasswordEncoder passwordEncoder,
                                      LoginSecurityConfigService loginSecurityConfigService,
                                      PasswordPolicyService passwordPolicyService,
                                      PasswordHistoryService passwordHistoryService,
                                      I18nMessageResolver i18nMessageResolver) {
        this.deptMapper = deptMapper;
        this.postMapper = postMapper;
        this.roleMapper = roleMapper;
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.userPostMapper = userPostMapper;
        this.passwordEncoder = passwordEncoder;
        this.loginSecurityConfigService = loginSecurityConfigService;
        this.passwordPolicyService = passwordPolicyService;
        this.passwordHistoryService = passwordHistoryService;
        this.i18nMessageResolver = i18nMessageResolver;
    }

    @Override
    public TenantProvisionVO bootstrap(TenantEntity tenant, TenantBootstrapOptions options) {
        if (tenant == null || !StringUtils.hasText(tenant.getId())) {
            throw new BusinessException("error.entity.not_found", "entity.name.tenant");
        }
        TenantBootstrapOptions opts = options == null ? new TenantBootstrapOptions() : options;
        String tenantId = tenant.getId();
        String tenantName = tenant.getTenantName();
        boolean organization = TenantType.isOrganization(tenant.getTenantType());
        String roleCode = organization ? CODE_ORG_ADMIN : CODE_TENANT_ADMIN;
        String adminRoleName = i18nMessageResolver.resolve(
                organization ? "tenant.bootstrap.org_admin_role" : "tenant.bootstrap.admin_role");
        String adminPostName = i18nMessageResolver.resolve(
                organization ? "tenant.bootstrap.org_admin_post" : "tenant.bootstrap.admin_post");

        String adminUsername = resolveAdminUsername(opts.getAdminUsername(), organization);
        String rootDeptCode = resolveDeptCode(opts.getRootDeptCode());
        String rootDeptName = StringUtils.hasText(opts.getRootDeptName())
                ? opts.getRootDeptName().trim()
                : tenantName;

        String initialPassword = loginSecurityConfigService.requireInitialPassword(tenantId);
        passwordPolicyService.validate(tenantId, initialPassword);

        return TenantScopeRunner.runIgnoringTenant(() -> {
            DeptEntity dept = createRootDept(tenantId, rootDeptName, rootDeptCode);
            PostEntity post = createAdminPost(tenantId, roleCode, adminPostName);
            RoleEntity role = createAdminRole(tenantId, roleCode, adminRoleName);
            UserEntity user = createAdminUser(tenantId, dept.getId(), adminUsername, adminRoleName, initialPassword);
            bindUserRole(tenantId, user.getId(), role.getId());
            bindUserPost(tenantId, user.getId(), post.getId());

            boolean forceChange = loginSecurityConfigService.isForceChangeInitial(tenantId);
            TenantProvisionVO vo = new TenantProvisionVO();
            vo.setTenantId(tenantId);
            vo.setTenantCode(tenant.getTenantCode());
            vo.setTenantName(tenantName);
            vo.setAdminUsername(adminUsername);
            vo.setInitialPassword(initialPassword);
            vo.setRootDeptCode(rootDeptCode);
            vo.setRootDeptName(dept.getDeptName());
            vo.setMustChangePassword(forceChange);
            return vo;
        });
    }

    private String resolveAdminUsername(String raw, boolean organization) {
        if (!StringUtils.hasText(raw)) {
            return organization ? CODE_ORG_ADMIN : CODE_TENANT_ADMIN;
        }
        String username = raw.trim();
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new BusinessException("gen.tenant.adminUsername.invalid");
        }
        return username;
    }

    private String resolveDeptCode(String raw) {
        if (!StringUtils.hasText(raw)) {
            return DEFAULT_DEPT_CODE;
        }
        String code = raw.trim();
        if (!DEPT_CODE_PATTERN.matcher(code).matches()) {
            throw new BusinessException("gen.tenant.rootDeptCode.invalid");
        }
        return code;
    }

    private DeptEntity createRootDept(String tenantId, String deptName, String deptCode) {
        DeptEntity dept = new DeptEntity();
        dept.setId(IdGeneratorUtils.nextIdStr());
        dept.setTenantId(tenantId);
        dept.setParentId(CommonConstants.TREE_ROOT_PARENT_ID);
        dept.setAncestors(CommonConstants.TREE_ROOT_PARENT_ID);
        dept.setDeptName(deptName);
        dept.setDeptCode(deptCode);
        dept.setSortNo(0);
        dept.setStatus(CommonConstants.STATUS_NORMAL);
        dept.setRemark(i18nMessageResolver.resolve("tenant.bootstrap.remark.root_dept"));
        AuditFieldHelper.fillInsert(dept);
        deptMapper.insert(dept);
        return dept;
    }

    private PostEntity createAdminPost(String tenantId, String postCode, String postName) {
        PostEntity post = new PostEntity();
        post.setId(IdGeneratorUtils.nextIdStr());
        post.setTenantId(tenantId);
        post.setPostName(postName);
        post.setPostCode(postCode);
        post.setSortNo(0);
        post.setStatus(CommonConstants.STATUS_NORMAL);
        post.setRemark(i18nMessageResolver.resolve("tenant.bootstrap.remark.admin_post"));
        AuditFieldHelper.fillInsert(post);
        postMapper.insert(post);
        return post;
    }

    private RoleEntity createAdminRole(String tenantId, String roleCode, String roleName) {
        RoleEntity role = new RoleEntity();
        role.setId(IdGeneratorUtils.nextIdStr());
        role.setTenantId(tenantId);
        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setDataScope(DataScopeEnum.ALL.getCode());
        role.setSortNo(1);
        role.setStatus(CommonConstants.STATUS_NORMAL);
        role.setIsSuperRole(CommonConstants.YES);
        role.setRemark(i18nMessageResolver.resolve("tenant.bootstrap.remark.admin_role"));
        AuditFieldHelper.fillInsert(role);
        roleMapper.insert(role);
        return role;
    }

    private UserEntity createAdminUser(String tenantId,
                                       String deptId,
                                       String username,
                                       String displayName,
                                       String rawPassword) {
        UserEntity existing = userMapper.selectByTenantAndUsername(tenantId, username);
        if (existing != null) {
            throw new BusinessException("error.user.username_exists");
        }
        UserEntity user = new UserEntity();
        user.setId(IdGeneratorUtils.nextIdStr());
        user.setTenantId(tenantId);
        user.setDeptId(deptId);
        user.setUsername(username);
        user.setNickname(displayName);
        user.setRealName(displayName);
        user.setUserType(USER_TYPE_SYSTEM);
        user.setGender(CommonConstants.GENDER_UNKNOWN);
        user.setStatus(CommonConstants.STATUS_NORMAL);
        user.setIsSuperAdmin(CommonConstants.NO);
        user.setLoginFailCount(0);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setPwdUpdatedAt(LocalDateTime.now());
        user.setMustChangePassword(
                loginSecurityConfigService.isForceChangeInitial(tenantId) ? CommonConstants.YES : CommonConstants.NO);
        user.setMfaEnabled(CommonConstants.NO);
        user.setRemark(i18nMessageResolver.resolve("tenant.bootstrap.remark.admin_user"));
        AuditFieldHelper.fillInsert(user);
        userMapper.insert(user);
        passwordHistoryService.record(tenantId, user.getId(), user.getPasswordHash());
        return user;
    }

    private void bindUserRole(String tenantId, String userId, String roleId) {
        UserRoleEntity bind = new UserRoleEntity();
        bind.setTenantId(tenantId);
        bind.setUserId(userId);
        bind.setRoleId(roleId);
        userRoleMapper.insert(bind);
    }

    private void bindUserPost(String tenantId, String userId, String postId) {
        UserPostEntity bind = new UserPostEntity();
        bind.setTenantId(tenantId);
        bind.setUserId(userId);
        bind.setPostId(postId);
        userPostMapper.insert(bind);
    }
}

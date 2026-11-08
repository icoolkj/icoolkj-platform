package com.icoolkj.platform.system.user.service.impl;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.enums.StatusEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.security.crypto.PasswordTransportCipher;
import com.icoolkj.platform.framework.security.service.LoginSessionService;
import com.icoolkj.platform.system.dept.domain.DeptEntity;
import com.icoolkj.platform.system.post.domain.PostEntity;
import com.icoolkj.platform.system.role.domain.RoleEntity;
import com.icoolkj.platform.system.tenant.domain.TenantEntity;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.user.domain.UserPostEntity;
import com.icoolkj.platform.system.auth.dto.ChangePasswordRequest;
import com.icoolkj.platform.system.user.dto.UserAvatarBindRequest;
import com.icoolkj.platform.system.user.dto.UserProfileUpdateRequest;
import com.icoolkj.platform.system.dept.mapper.DeptMapper;
import com.icoolkj.platform.system.post.mapper.PostMapper;
import com.icoolkj.platform.system.role.mapper.RoleMapper;
import com.icoolkj.platform.system.tenant.mapper.TenantMapper;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.user.mapper.UserPostMapper;
import com.icoolkj.platform.system.user.mapper.UserRoleMapper;
import com.icoolkj.platform.system.user.service.PasswordHistoryService;
import com.icoolkj.platform.system.user.service.PasswordPolicyService;
import com.icoolkj.platform.system.user.service.UserProfileService;
import com.icoolkj.platform.system.user.vo.UserProfilePostVO;
import com.icoolkj.platform.system.user.vo.UserProfileRoleVO;
import com.icoolkj.platform.system.user.vo.UserProfileVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final TenantMapper tenantMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final UserPostMapper userPostMapper;
    private final PostMapper postMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;
    private final PasswordHistoryService passwordHistoryService;
    private final LoginSessionService loginSessionService;
    private final PasswordTransportCipher passwordTransportCipher;

    public UserProfileServiceImpl(UserMapper userMapper,
                                  DeptMapper deptMapper,
                                  TenantMapper tenantMapper,
                                  UserRoleMapper userRoleMapper,
                                  RoleMapper roleMapper,
                                  UserPostMapper userPostMapper,
                                  PostMapper postMapper,
                                  PasswordEncoder passwordEncoder,
                                  PasswordPolicyService passwordPolicyService,
                                  PasswordHistoryService passwordHistoryService,
                                  LoginSessionService loginSessionService,
                                  PasswordTransportCipher passwordTransportCipher) {
        this.userMapper = userMapper;
        this.deptMapper = deptMapper;
        this.tenantMapper = tenantMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.userPostMapper = userPostMapper;
        this.postMapper = postMapper;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyService = passwordPolicyService;
        this.passwordHistoryService = passwordHistoryService;
        this.loginSessionService = loginSessionService;
        this.passwordTransportCipher = passwordTransportCipher;
    }

    @Override
    public UserProfileVO getProfile(String tenantId, String tenantCode, String userId) {
        UserEntity user = requireUser(userId, tenantId);
        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setGender(user.getGender());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setAvatarFileId(user.getAvatarFileId());
        vo.setAvatarUrlSm(user.getAvatarUrlSm());
        vo.setAvatarUrlMd(user.getAvatarUrl());
        vo.setAvatarUrlLg(user.getAvatarUrlLg());
        vo.setTenantId(user.getTenantId());
        vo.setTenantCode(tenantCode);
        vo.setTenantName(resolveTenantName(user.getTenantId()));
        vo.setDeptId(user.getDeptId());
        vo.setDeptName(resolveDeptName(user.getDeptId()));
        vo.setRoles(loadRoles(tenantId, userId));
        vo.setPosts(loadPosts(tenantId, userId));
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setLastLoginAt(user.getLastLoginAt());
        vo.setPwdUpdatedAt(user.getPwdUpdatedAt());
        vo.setMustChangePassword(user.getMustChangePassword());
        return vo;
    }

    @Override
    public UserProfileVO updateProfile(String tenantId, String tenantCode, String userId, UserProfileUpdateRequest request) {
        UserEntity user = requireUser(userId, tenantId);
        user.setNickname(request.getNickname().trim());
        user.setRealName(trimToNull(request.getRealName()));
        user.setEmail(trimToNull(request.getEmail()));
        user.setPhone(trimToNull(request.getPhone()));
        user.setGender(request.getGender().trim());
        userMapper.updateById(user);
        return getProfile(tenantId, tenantCode, userId);
    }

    @Override
    public UserProfileVO applyAvatar(String tenantId, String tenantCode, String userId, UserAvatarBindRequest request) {
        UserEntity user = requireUser(userId, tenantId);
        user.setAvatarFileId(trimToNull(request.getAvatarFileId()));
        user.setAvatarUrl(trimToNull(request.getAvatarUrl()));
        user.setAvatarUrlSm(trimToNull(request.getAvatarUrlSm()));
        user.setAvatarUrlLg(trimToNull(request.getAvatarUrlLg()));
        userMapper.updateById(user);
        return getProfile(tenantId, tenantCode, userId);
    }

    @Override
    public void changePassword(String tenantId, String userId, ChangePasswordRequest request) {
        UserEntity user = requireUser(userId, tenantId);
        String oldPassword = passwordTransportCipher.unwrap(request.getOldPassword());
        String newPassword = passwordTransportCipher.unwrap(request.getNewPassword());
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException("error.auth.old_password_incorrect");
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new BusinessException("error.auth.new_password_same");
        }
        passwordPolicyService.validate(tenantId, newPassword);
        passwordHistoryService.assertNotReused(tenantId, userId, user.getPasswordHash(), newPassword);
        String previousHash = user.getPasswordHash();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPwdUpdatedAt(LocalDateTime.now());
        user.setMustChangePassword(CommonConstants.NO);
        userMapper.updateById(user);
        passwordHistoryService.record(tenantId, userId, previousHash);
        loginSessionService.removeAllUserSessions(tenantId, userId);
    }

    private UserEntity requireUser(String userId, String tenantId) {
        UserEntity entity = userMapper.selectById(userId);
        if (entity == null || !tenantId.equals(entity.getTenantId())) {
            throw new BusinessException("error.entity.not_found", "entity.name.user");
        }
        return entity;
    }

    private String resolveTenantName(String tenantId) {
        if (!StringUtils.hasText(tenantId)) {
            return null;
        }
        TenantEntity tenant = tenantMapper.selectById(tenantId);
        return tenant == null ? null : tenant.getTenantName();
    }

    private String resolveDeptName(String deptId) {
        if (!StringUtils.hasText(deptId)) {
            return null;
        }
        DeptEntity dept = deptMapper.selectById(deptId);
        return dept == null ? null : dept.getDeptName();
    }

    private List<UserProfileRoleVO> loadRoles(String tenantId, String userId) {
        List<String> roleIds = userRoleMapper.selectRoleIdsByTenantAndUser(tenantId, userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        List<UserProfileRoleVO> roles = new ArrayList<>();
        for (String roleId : roleIds) {
            RoleEntity role = roleMapper.selectById(roleId);
            if (role == null || !StatusEnum.isNormal(role.getStatus())) {
                continue;
            }
            UserProfileRoleVO item = new UserProfileRoleVO();
            item.setId(role.getId());
            item.setRoleName(role.getRoleName());
            item.setRoleCode(role.getRoleCode());
            roles.add(item);
        }
        return roles;
    }

    private List<UserProfilePostVO> loadPosts(String tenantId, String userId) {
        UserPostEntity query = new UserPostEntity();
        query.setTenantId(tenantId);
        query.setUserId(userId);
        List<UserPostEntity> links = userPostMapper.selectList(query);
        if (CollectionUtils.isEmpty(links)) {
            return Collections.emptyList();
        }
        List<UserProfilePostVO> posts = new ArrayList<>();
        for (UserPostEntity link : links) {
            PostEntity post = postMapper.selectById(link.getPostId());
            if (post == null || !StatusEnum.isNormal(post.getStatus())) {
                continue;
            }
            UserProfilePostVO item = new UserProfilePostVO();
            item.setId(post.getId());
            item.setPostName(post.getPostName());
            item.setPostCode(post.getPostCode());
            posts.add(item);
        }
        return posts;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}

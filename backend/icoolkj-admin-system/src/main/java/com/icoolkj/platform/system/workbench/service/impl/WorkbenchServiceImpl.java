package com.icoolkj.platform.system.workbench.service.impl;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.util.LocaleListJoin;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.system.dept.dto.DeptPageQuery;
import com.icoolkj.platform.system.post.dto.PostPageQuery;
import com.icoolkj.platform.system.role.dto.RolePageQuery;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessageInboxPageQuery;
import com.icoolkj.platform.system.tenant.dto.TenantPageQuery;
import com.icoolkj.platform.system.user.dto.UserPageQuery;
import com.icoolkj.platform.system.dept.mapper.DeptMapper;
import com.icoolkj.platform.system.post.mapper.PostMapper;
import com.icoolkj.platform.system.role.mapper.RoleMapper;
import com.icoolkj.platform.system.sitemessage.mapper.SiteMessageRecipientMapper;
import com.icoolkj.platform.system.tenant.mapper.TenantMapper;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.workbench.mapper.WorkbenchMapper;
import com.icoolkj.platform.system.user.service.UserProfileService;
import com.icoolkj.platform.system.workbench.service.WorkbenchService;
import com.icoolkj.platform.system.user.vo.UserProfilePostVO;
import com.icoolkj.platform.system.user.vo.UserProfileVO;
import com.icoolkj.platform.system.workbench.vo.WorkbenchHeaderVO;
import com.icoolkj.platform.system.workbench.vo.WorkbenchStatVO;
import com.icoolkj.platform.system.workbench.vo.WorkbenchVO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 租户工作台服务实现 */
@Service
public class WorkbenchServiceImpl implements WorkbenchService {

    private final UserProfileService userProfileService;
    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final RoleMapper roleMapper;
    private final PostMapper postMapper;
    private final TenantMapper tenantMapper;
    private final SiteMessageRecipientMapper siteMessageRecipientMapper;
    private final WorkbenchMapper workbenchMapper;

    public WorkbenchServiceImpl(UserProfileService userProfileService,
                                UserMapper userMapper,
                                DeptMapper deptMapper,
                                RoleMapper roleMapper,
                                PostMapper postMapper,
                                TenantMapper tenantMapper,
                                SiteMessageRecipientMapper siteMessageRecipientMapper,
                                WorkbenchMapper workbenchMapper) {
        this.userProfileService = userProfileService;
        this.userMapper = userMapper;
        this.deptMapper = deptMapper;
        this.roleMapper = roleMapper;
        this.postMapper = postMapper;
        this.tenantMapper = tenantMapper;
        this.siteMessageRecipientMapper = siteMessageRecipientMapper;
        this.workbenchMapper = workbenchMapper;
    }

    @Override
    public WorkbenchVO load() {
        String tenantId = TenantQueryScope.requiredOperatorTenantId();
        LoginUser loginUser = SecurityContextHelper.getLoginUser();
        String userId = loginUser != null ? loginUser.getUserId() : SecurityContextHelper.getUserId();
        String tenantCode = loginUser != null ? loginUser.getTenantCode() : null;
        boolean platformOperator = TenantContextHolder.isPlatform();

        UserProfileVO profile = userProfileService.getProfile(tenantId, tenantCode, userId);

        WorkbenchVO vo = new WorkbenchVO();
        vo.setHeader(buildHeader(profile, platformOperator));
        vo.setStats(buildStats(platformOperator, tenantId));
        vo.setUnreadSiteMessageCount(countUnreadMessages(tenantId, userId));
        vo.setRecentLoginLogs(TenantScopeRunner.runIgnoringTenant(() ->
                workbenchMapper.selectRecentLoginLogs(scopeTenantId(platformOperator, tenantId), 6)));
        vo.setRecentOperationLogs(TenantScopeRunner.runIgnoringTenant(() ->
                workbenchMapper.selectRecentOperationLogs(scopeTenantId(platformOperator, tenantId), 6)));
        return vo;
    }

    private WorkbenchHeaderVO buildHeader(UserProfileVO profile, boolean platformOperator) {
        WorkbenchHeaderVO header = new WorkbenchHeaderVO();
        header.setNickname(StringUtils.hasText(profile.getNickname()) ? profile.getNickname() : profile.getUsername());
        header.setTenantId(profile.getTenantId());
        header.setTenantCode(profile.getTenantCode());
        header.setTenantName(resolveTenantName(profile, platformOperator));
        header.setDeptName(profile.getDeptName());
        header.setPostLabel(buildPostLabel(profile.getPosts()));
        header.setPlatformOperator(platformOperator);
        return header;
    }

    private String resolveTenantName(UserProfileVO profile, boolean platformOperator) {
        if (platformOperator) {
            // 空名称由前端 gp('scopePlatform') / platformTenantLabel 补文案，禁止硬编码「平台」
            return StringUtils.hasText(profile.getTenantName()) ? profile.getTenantName() : null;
        }
        return StringUtils.hasText(profile.getTenantName()) ? profile.getTenantName() : profile.getTenantCode();
    }

    private String buildPostLabel(List<UserProfilePostVO> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return null;
        }
        String joined = LocaleListJoin.join(posts.stream()
                .map(UserProfilePostVO::getPostName)
                .filter(StringUtils::hasText)
                .toList());
        return StringUtils.hasText(joined) ? joined : null;
    }

    private List<WorkbenchStatVO> buildStats(boolean platformOperator, String tenantId) {
        List<WorkbenchStatVO> stats = new ArrayList<>();
        if (platformOperator) {
            stats.add(stat("tenantCount", countTenants()));
            stats.add(stat("userCount", countUsers(null)));
            stats.add(stat("todayLogins", countTodayLogins(null)));
            stats.add(stat("roleCount", countRoles(TenantConstants.PLATFORM_TENANT_ID)));
        } else {
            stats.add(stat("userCount", countUsers(tenantId)));
            stats.add(stat("deptCount", countDepts(tenantId)));
            stats.add(stat("roleCount", countRoles(tenantId)));
            stats.add(stat("postCount", countPosts(tenantId)));
        }
        return stats;
    }

    private WorkbenchStatVO stat(String key, long value) {
        WorkbenchStatVO item = new WorkbenchStatVO();
        item.setKey(key);
        item.setValue(value);
        return item;
    }

    private String scopeTenantId(boolean platformOperator, String tenantId) {
        return platformOperator ? null : tenantId;
    }

    private long countTenants() {
        TenantPageQuery query = new TenantPageQuery();
        return TenantScopeRunner.runIgnoringTenant(() -> tenantMapper.countByQuery(query));
    }

    private long countUsers(String tenantId) {
        UserPageQuery query = new UserPageQuery();
        if (StringUtils.hasText(tenantId)) {
            query.setTenantId(tenantId);
        }
        return TenantScopeRunner.runIgnoringTenant(() -> {
            if (!StringUtils.hasText(tenantId)) {
                TenantQueryScope.applyPageQuery(query);
            }
            return userMapper.countByQuery(query);
        });
    }

    private long countDepts(String tenantId) {
        DeptPageQuery query = new DeptPageQuery();
        query.setTenantId(tenantId);
        return TenantScopeRunner.runIgnoringTenant(() -> deptMapper.countByQuery(query));
    }

    private long countRoles(String tenantId) {
        RolePageQuery query = new RolePageQuery();
        query.setTenantId(tenantId);
        return TenantScopeRunner.runIgnoringTenant(() -> roleMapper.countByQuery(query));
    }

    private long countPosts(String tenantId) {
        PostPageQuery query = new PostPageQuery();
        query.setTenantId(tenantId);
        return TenantScopeRunner.runIgnoringTenant(() -> postMapper.countByQuery(query));
    }

    private long countTodayLogins(String tenantId) {
        return TenantScopeRunner.runIgnoringTenant(() -> workbenchMapper.countTodayLogins(tenantId));
    }

    private long countUnreadMessages(String tenantId, String userId) {
        SiteMessageInboxPageQuery query = new SiteMessageInboxPageQuery();
        query.setTenantId(tenantId);
        query.setUserId(userId);
        query.setReadStatus(CommonConstants.NO);
        return siteMessageRecipientMapper.countInboxByQuery(query);
    }
}

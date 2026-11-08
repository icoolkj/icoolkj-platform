package com.icoolkj.platform.system.user.service.impl;

import com.icoolkj.platform.common.constant.ConfigKeys;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.constant.UserPreferenceKeys;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.i18n.LocaleResolver;
import com.icoolkj.platform.framework.auth.PermissionService;
import com.icoolkj.platform.system.user.support.LayoutPreferenceHelper;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.framework.security.service.LoginSessionService;
import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.user.domain.UserPreferenceEntity;
import com.icoolkj.platform.system.user.dto.UserPreferenceUpdateRequest;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.user.mapper.UserPreferenceMapper;
import com.icoolkj.platform.system.config.service.ConfigService;
import com.icoolkj.platform.system.user.service.UserPreferenceService;
import com.icoolkj.platform.system.auth.vo.AuthMeVO;
import com.icoolkj.platform.system.user.vo.LayoutPreferenceVO;
import com.icoolkj.platform.system.user.vo.UserPreferenceVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.icoolkj.platform.system.auth.service.impl.LoginSecurityConfigService;

@Service
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceMapper userPreferenceMapper;
    private final ConfigService configService;
    private final UserMapper userMapper;
    private final PermissionService permissionService;
    private final LoginSessionService loginSessionService;
    private final LoginSecurityConfigService loginSecurityConfigService;

    public UserPreferenceServiceImpl(UserPreferenceMapper userPreferenceMapper,
                                       ConfigService configService,
                                       UserMapper userMapper,
                                       PermissionService permissionService,
                                       LoginSessionService loginSessionService,
                                       LoginSecurityConfigService loginSecurityConfigService) {
        this.userPreferenceMapper = userPreferenceMapper;
        this.configService = configService;
        this.userMapper = userMapper;
        this.permissionService = permissionService;
        this.loginSessionService = loginSessionService;
        this.loginSecurityConfigService = loginSecurityConfigService;
    }

    @Override
    public UserPreferenceVO getPreference(String tenantId, String userId) {
        Map<String, String> map = loadPrefMap(userId);
        UserPreferenceVO vo = new UserPreferenceVO();
        vo.setLocale(resolveLocale(tenantId, map.get(UserPreferenceKeys.UI_LOCALE)));
        vo.setTheme(resolveTheme(map.get(UserPreferenceKeys.UI_THEME)));
        vo.setLayout(LayoutPreferenceHelper.parse(map.get(UserPreferenceKeys.UI_LAYOUT)));
        return vo;
    }

    @Override
    public UserPreferenceVO updatePreference(String tenantId, String userId, UserPreferenceUpdateRequest request) {
        if (StringUtils.hasText(request.getLocale())) {
            upsert(tenantId, userId, UserPreferenceKeys.UI_LOCALE, LocaleResolver.normalize(request.getLocale()));
        }
        if (StringUtils.hasText(request.getTheme())) {
            upsert(tenantId, userId, UserPreferenceKeys.UI_THEME, request.getTheme().trim());
        }
        if (request.getLayout() != null) {
            Map<String, String> map = loadPrefMap(userId);
            LayoutPreferenceVO current = LayoutPreferenceHelper.parse(map.get(UserPreferenceKeys.UI_LAYOUT));
            LayoutPreferenceHelper.merge(current, request.getLayout());
            upsert(tenantId, userId, UserPreferenceKeys.UI_LAYOUT, LayoutPreferenceHelper.toJson(current));
        }
        return getPreference(tenantId, userId);
    }

    @Override
    public AuthMeVO buildAuthMe(LoginUser loginUser) {
        AuthMeVO vo = new AuthMeVO();
        vo.setUserId(loginUser.getUserId());
        vo.setUsername(loginUser.getUsername());
        vo.setTenantId(loginUser.getTenantId());
        vo.setTenantCode(loginUser.getTenantCode());
        UserEntity user = userMapper.selectById(loginUser.getUserId());
        boolean superAdmin = user != null && YesNoEnum.isYes(user.getIsSuperAdmin());
        vo.setPermissions(permissionService.loadPermissionCodes(
                loginUser.getTenantId(), loginUser.getUserId(), superAdmin));
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatarFileId(user.getAvatarFileId());
            vo.setAvatarUrl(user.getAvatarUrl());
            vo.setAvatarUrlSm(user.getAvatarUrlSm());
            vo.setAvatarUrlLg(user.getAvatarUrlLg());
            // 与登录会话一致：用户标志 ∧ 租户/平台 security.password.force-change-initial
            vo.setMustChangePassword(loginSecurityConfigService.isMustChangePasswordRequired(
                    loginUser.getTenantId(), user.getMustChangePassword()));
            vo.setMfaEnabled(YesNoEnum.isYes(user.getMfaEnabled()));
        } else {
            vo.setMustChangePassword(Boolean.FALSE);
            vo.setMfaEnabled(Boolean.FALSE);
        }
        boolean mustEnrollMfa = false;
        if (StringUtils.hasText(loginUser.getSessionId())) {
            mustEnrollMfa = loginSessionService.getSession(loginUser.getSessionId())
                    .map(s -> s.isMustEnrollMfa())
                    .orElse(false);
        }
        vo.setMustEnrollMfa(mustEnrollMfa);
        UserPreferenceVO pref = getPreference(loginUser.getTenantId(), loginUser.getUserId());
        vo.setLocale(pref.getLocale());
        vo.setTheme(pref.getTheme());
        vo.setLayout(pref.getLayout());
        return vo;
    }

    private Map<String, String> loadPrefMap(String userId) {
        List<UserPreferenceEntity> list = userPreferenceMapper.selectByUserId(userId);
        Map<String, String> map = new HashMap<>();
        if (list != null) {
            for (UserPreferenceEntity item : list) {
                map.put(item.getPrefKey(), item.getPrefValue());
            }
        }
        return map;
    }

    private void upsert(String tenantId, String userId, String key, String value) {
        UserPreferenceEntity entity = new UserPreferenceEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(tenantId));
        entity.setUserId(userId);
        entity.setPrefKey(key);
        entity.setPrefValue(value);
        userPreferenceMapper.upsert(entity);
    }

    private String resolveLocale(String tenantId, String stored) {
        if (StringUtils.hasText(stored)) {
            return LocaleResolver.normalize(stored);
        }
        try {
            String scopeTenantId = StringUtils.hasText(tenantId) ? tenantId : TenantConstants.PLATFORM_TENANT_ID;
            String defaultLocale = configService.getString(
                    scopeTenantId,
                    ConfigKeys.I18N_DEFAULT_LOCALE,
                    LocaleResolver.LOCALE_ZH_CN);
            return LocaleResolver.normalize(defaultLocale);
        } catch (Exception ex) {
            return LocaleResolver.LOCALE_ZH_CN;
        }
    }

    private String resolveTheme(String stored) {
        if (StringUtils.hasText(stored)) {
            return stored.trim();
        }
        return UserPreferenceKeys.DEFAULT_THEME;
    }
}

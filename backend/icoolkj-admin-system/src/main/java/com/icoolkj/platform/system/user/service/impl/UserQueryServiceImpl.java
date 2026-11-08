package com.icoolkj.platform.system.user.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.user.service.UserQueryService;

import java.util.Optional;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserMapper userMapper;

    public UserQueryServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Optional<UserEntity> findLoginUser(String tenantId, String username) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(username)) {
            return Optional.empty();
        }

        UserEntity user = userMapper.selectByTenantAndUsername(tenantId, username.trim());
        return Optional.ofNullable(user);
    }
}

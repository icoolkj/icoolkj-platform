package com.icoolkj.platform.system.user.service;

import java.util.Optional;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.system.user.domain.UserEntity;

/**
 * Read-side user queries.
 */
public interface UserQueryService {

    Optional<UserEntity> findLoginUser(String tenantId, String username);
}

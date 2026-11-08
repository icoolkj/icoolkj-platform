package com.icoolkj.platform.system.user.service.impl;

import com.icoolkj.platform.common.constant.ConfigKeys;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.system.user.domain.UserPasswordHistoryEntity;
import com.icoolkj.platform.system.user.mapper.UserPasswordHistoryMapper;
import com.icoolkj.platform.system.config.service.ConfigService;
import com.icoolkj.platform.system.user.service.PasswordHistoryService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PasswordHistoryServiceImpl implements PasswordHistoryService {

    private static final int DEFAULT_HISTORY_COUNT = 5;

    private final UserPasswordHistoryMapper userPasswordHistoryMapper;
    private final ConfigService configService;
    private final PasswordEncoder passwordEncoder;

    public PasswordHistoryServiceImpl(UserPasswordHistoryMapper userPasswordHistoryMapper,
                                      ConfigService configService,
                                      PasswordEncoder passwordEncoder) {
        this.userPasswordHistoryMapper = userPasswordHistoryMapper;
        this.configService = configService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void assertNotReused(String tenantId, String userId, String currentPasswordHash, String rawPassword) {
        int historyCount = historyCount(tenantId);
        if (historyCount <= 0 || !StringUtils.hasText(rawPassword)) {
            return;
        }
        if (StringUtils.hasText(currentPasswordHash) && passwordEncoder.matches(rawPassword, currentPasswordHash)) {
            throw new BusinessException("error.auth.password_reused");
        }
        List<UserPasswordHistoryEntity> recent =
                userPasswordHistoryMapper.selectRecent(tenantId, userId, historyCount);
        for (UserPasswordHistoryEntity row : recent) {
            if (row != null && StringUtils.hasText(row.getPasswordHash())
                    && passwordEncoder.matches(rawPassword, row.getPasswordHash())) {
                throw new BusinessException("error.auth.password_reused");
            }
        }
    }

    @Override
    public void record(String tenantId, String userId, String passwordHash) {
        int historyCount = historyCount(tenantId);
        if (historyCount <= 0 || !StringUtils.hasText(passwordHash)) {
            return;
        }
        UserPasswordHistoryEntity entity = new UserPasswordHistoryEntity();
        entity.setId(IdGeneratorUtils.nextIdStr());
        entity.setTenantId(tenantId);
        entity.setUserId(userId);
        entity.setPasswordHash(passwordHash);
        entity.setCreatedAt(LocalDateTime.now());
        userPasswordHistoryMapper.insert(entity);

        List<UserPasswordHistoryEntity> recent =
                userPasswordHistoryMapper.selectRecent(tenantId, userId, historyCount + 50);
        for (int i = historyCount; i < recent.size(); i++) {
            userPasswordHistoryMapper.deleteById(recent.get(i).getId());
        }
    }

    private int historyCount(String tenantId) {
        return configService.getInt(tenantId, ConfigKeys.PASSWORD_HISTORY_COUNT, DEFAULT_HISTORY_COUNT);
    }
}

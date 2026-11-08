package com.icoolkj.platform.system.loginlog.service.impl;

import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.common.enums.LoginStatusEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.audit.AuditIntegrityService;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.system.loginlog.convert.LoginLogConvert;
import com.icoolkj.platform.system.loginlog.dto.LoginLogPageQuery;
import com.icoolkj.platform.system.loginlog.vo.LoginLogVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.icoolkj.platform.system.loginlog.domain.LoginLogEntity;
import com.icoolkj.platform.system.loginlog.mapper.LoginLogMapper;
import com.icoolkj.platform.system.loginlog.service.LoginLogService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class LoginLogServiceImpl implements LoginLogService {

    private static final String LOGIN_TYPE_PASSWORD = "password";
    private static final int MAX_MESSAGE_LENGTH = 500;
    private static final int MAX_USER_AGENT_LENGTH = 500;

    private final LoginLogMapper loginLogMapper;
    private final AuditIntegrityService auditIntegrityService;

    public LoginLogServiceImpl(LoginLogMapper loginLogMapper,
                               AuditIntegrityService auditIntegrityService) {
        this.loginLogMapper = loginLogMapper;
        this.auditIntegrityService = auditIntegrityService;
    }

    @Override
    public void recordSuccess(String tenantId, String username, String message, String clientIp, String userAgent) {
        save(tenantId, username, LoginStatusEnum.SUCCESS.getCode(), message, clientIp, userAgent);
    }

    @Override
    public void recordFailure(String tenantId, String username, String message, String clientIp, String userAgent) {
        save(tenantId, username, LoginStatusEnum.FAIL.getCode(), message, clientIp, userAgent);
    }

    private void save(String tenantId,
                      String username,
                      String loginStatus,
                      String message,
                      String clientIp,
                      String userAgent) {
        LoginLogEntity entity = new LoginLogEntity();
        entity.setId(IdGeneratorUtils.nextIdStr());
        entity.setTenantId(tenantId);
        entity.setUsername(username);
        entity.setLoginType(LOGIN_TYPE_PASSWORD);
        entity.setLoginStatus(loginStatus);
        entity.setMessage(truncate(message, MAX_MESSAGE_LENGTH));
        entity.setIp(truncate(clientIp, 64));
        entity.setUserAgent(truncate(userAgent, MAX_USER_AGENT_LENGTH));
        entity.setLoginAt(LocalDateTime.now());
        String payload = AuditIntegrityService.joinPayload(
                entity.getId(), entity.getTenantId(), entity.getUsername(), entity.getLoginStatus(),
                entity.getIp(), entity.getLoginAt());
        AuditIntegrityService.ChainHashes hashes = auditIntegrityService.next(
                AuditIntegrityService.CHAIN_LOGIN,
                tenantId,
                payload,
                () -> loginLogMapper.selectLatestRowHash(tenantId));
        entity.setPrevHash(hashes.prevHash());
        entity.setRowHash(hashes.rowHash());
        loginLogMapper.insert(entity);
    }

    private String truncate(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    @Override
    public PageResult<LoginLogVO> page(LoginLogPageQuery query) {
        TenantQueryScope.applyPageQuery(query);
        long total = loginLogMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<LoginLogEntity> list = loginLogMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        List<LoginLogVO> rows = list.stream().map(LoginLogConvert::toVo).toList();
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public LoginLogVO getById(String id) {
        LoginLogEntity entity = loginLogMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.login_log");
        }
        TenantQueryScope.assertReadable(entity.getTenantId());
        return LoginLogConvert.toVo(entity);
    }

    @Override
    public int purgeBefore(LocalDateTime before) {
        return loginLogMapper.purgeBefore(before);
    }

    @Override
    public int archiveThenPurge(LocalDateTime before) {
        List<String> tenantIds = loginLogMapper.selectTenantIdsBefore(before);
        int archived = loginLogMapper.archiveBefore(before);
        if (archived > 0) {
            loginLogMapper.purgeBefore(before);
            for (String tenantId : tenantIds) {
                reanchorLiveChain(tenantId);
            }
        }
        return archived;
    }

    private void reanchorLiveChain(String tenantId) {
        List<LoginLogEntity> rows = loginLogMapper.selectHashedByTenantOrdered(tenantId);
        auditIntegrityService.reanchor(
                AuditIntegrityService.CHAIN_LOGIN,
                tenantId,
                rows,
                row -> AuditIntegrityService.joinPayload(
                        row.getId(), row.getTenantId(), row.getUsername(), row.getLoginStatus(),
                        row.getIp(), row.getLoginAt()),
                (row, hashes) -> loginLogMapper.updateHashes(row.getId(), hashes.prevHash(), hashes.rowHash()));
    }

    @Override
    public int purgeArchiveBefore(LocalDateTime before) {
        return loginLogMapper.purgeArchiveBefore(before);
    }
}

package com.icoolkj.platform.system.audit.service.impl;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.model.audit.AuditChainVerifyVO;
import com.icoolkj.platform.framework.audit.AuditIntegrityService;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.system.loginlog.domain.LoginLogEntity;
import com.icoolkj.platform.system.operationlog.domain.OperationLogEntity;
import com.icoolkj.platform.system.loginlog.mapper.LoginLogMapper;
import com.icoolkj.platform.system.operationlog.mapper.OperationLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

@Service
public class AuditChainVerifyService {

    private final OperationLogMapper operationLogMapper;
    private final LoginLogMapper loginLogMapper;

    public AuditChainVerifyService(OperationLogMapper operationLogMapper, LoginLogMapper loginLogMapper) {
        this.operationLogMapper = operationLogMapper;
        this.loginLogMapper = loginLogMapper;
    }

    public AuditChainVerifyVO verifyOperationChain() {
        String tenantId = resolveTenantId();
        OperationLogEntity query = new OperationLogEntity();
        query.setTenantId(tenantId);
        List<OperationLogEntity> rows = operationLogMapper.selectList(query);
        // Snowflake ids are time-ordered; id ASC matches insertion / chain construction.
        rows.sort(Comparator.comparing(OperationLogEntity::getId, Comparator.nullsLast(Comparator.naturalOrder())));

        AuditChainVerifyVO vo = baseVo(AuditIntegrityService.CHAIN_OPERATION, tenantId);
        String expectedPrev = AuditIntegrityService.GENESIS;
        for (OperationLogEntity row : rows) {
            if (!StringUtils.hasText(row.getRowHash())) {
                continue;
            }
            vo.setCheckedCount(vo.getCheckedCount() + 1);
            if (!expectedPrev.equals(nullToEmpty(row.getPrevHash()))) {
                return breakAt(vo, row.getId(), "prev_hash mismatch");
            }
            String payload = AuditIntegrityService.joinPayload(
                    row.getId(), row.getTenantId(), row.getModuleName(), row.getBusinessType(),
                    row.getOperatorId(), row.getOperationUrl(), row.getOperationStatus(),
                    row.getOperationAt());
            String expected = AuditIntegrityService.sha256Hex(row.getPrevHash() + "|" + payload);
            if (!expected.equals(row.getRowHash())) {
                return breakAt(vo, row.getId(), "row_hash mismatch");
            }
            expectedPrev = row.getRowHash();
        }
        vo.setMessage(vo.getCheckedCount() == 0 ? "no hashed rows" : "ok");
        return vo;
    }

    public AuditChainVerifyVO verifyLoginChain() {
        String tenantId = resolveTenantId();
        LoginLogEntity query = new LoginLogEntity();
        query.setTenantId(tenantId);
        List<LoginLogEntity> rows = loginLogMapper.selectList(query);
        rows.sort(Comparator.comparing(LoginLogEntity::getId, Comparator.nullsLast(Comparator.naturalOrder())));

        AuditChainVerifyVO vo = baseVo(AuditIntegrityService.CHAIN_LOGIN, tenantId);
        String expectedPrev = AuditIntegrityService.GENESIS;
        for (LoginLogEntity row : rows) {
            if (!StringUtils.hasText(row.getRowHash())) {
                continue;
            }
            vo.setCheckedCount(vo.getCheckedCount() + 1);
            if (!expectedPrev.equals(nullToEmpty(row.getPrevHash()))) {
                return breakAt(vo, row.getId(), "prev_hash mismatch");
            }
            String payload = AuditIntegrityService.joinPayload(
                    row.getId(), row.getTenantId(), row.getUsername(), row.getLoginStatus(),
                    row.getIp(), row.getLoginAt());
            String expected = AuditIntegrityService.sha256Hex(row.getPrevHash() + "|" + payload);
            if (!expected.equals(row.getRowHash())) {
                return breakAt(vo, row.getId(), "row_hash mismatch");
            }
            expectedPrev = row.getRowHash();
        }
        vo.setMessage(vo.getCheckedCount() == 0 ? "no hashed rows" : "ok");
        return vo;
    }

    private static String resolveTenantId() {
        String tenantId = SecurityContextHelper.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            return TenantConstants.PLATFORM_TENANT_ID;
        }
        return tenantId;
    }

    private static AuditChainVerifyVO baseVo(String chainName, String tenantId) {
        AuditChainVerifyVO vo = new AuditChainVerifyVO();
        vo.setChainName(chainName);
        vo.setTenantId(tenantId);
        vo.setCheckedCount(0);
        vo.setValid(true);
        return vo;
    }

    private static AuditChainVerifyVO breakAt(AuditChainVerifyVO vo, String breakId, String message) {
        vo.setValid(false);
        vo.setBreakId(breakId);
        vo.setMessage(message);
        return vo;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

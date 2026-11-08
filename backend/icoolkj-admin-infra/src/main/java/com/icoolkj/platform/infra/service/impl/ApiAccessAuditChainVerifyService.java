package com.icoolkj.platform.infra.service.impl;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.model.audit.AuditChainVerifyVO;
import com.icoolkj.platform.framework.audit.AuditIntegrityService;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.infra.domain.ApiAccessLogEntity;
import com.icoolkj.platform.infra.mapper.ApiAccessLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

@Service
public class ApiAccessAuditChainVerifyService {

    private final ApiAccessLogMapper apiAccessLogMapper;

    public ApiAccessAuditChainVerifyService(ApiAccessLogMapper apiAccessLogMapper) {
        this.apiAccessLogMapper = apiAccessLogMapper;
    }

    public AuditChainVerifyVO verifyApiAccessChain() {
        String tenantId = SecurityContextHelper.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            tenantId = TenantConstants.PLATFORM_TENANT_ID;
        }
        ApiAccessLogEntity query = new ApiAccessLogEntity();
        query.setTenantId(tenantId);
        List<ApiAccessLogEntity> rows = apiAccessLogMapper.selectList(query);
        rows.sort(Comparator.comparing(ApiAccessLogEntity::getId, Comparator.nullsLast(Comparator.naturalOrder())));

        AuditChainVerifyVO vo = new AuditChainVerifyVO();
        vo.setChainName(AuditIntegrityService.CHAIN_API_ACCESS);
        vo.setTenantId(tenantId);
        vo.setCheckedCount(0);
        vo.setValid(true);

        String expectedPrev = AuditIntegrityService.GENESIS;
        for (ApiAccessLogEntity row : rows) {
            if (!StringUtils.hasText(row.getRowHash())) {
                continue;
            }
            vo.setCheckedCount(vo.getCheckedCount() + 1);
            String prev = row.getPrevHash() == null ? "" : row.getPrevHash();
            if (!expectedPrev.equals(prev)) {
                vo.setValid(false);
                vo.setBreakId(row.getId());
                vo.setMessage("prev_hash mismatch");
                return vo;
            }
            String payload = AuditIntegrityService.joinPayload(
                    row.getId(), row.getTenantId(), row.getRequestUri(), row.getRequestMethod(),
                    row.getUserId(), row.getResponseCode(), row.getAccessedAt());
            String expected = AuditIntegrityService.sha256Hex(row.getPrevHash() + "|" + payload);
            if (!expected.equals(row.getRowHash())) {
                vo.setValid(false);
                vo.setBreakId(row.getId());
                vo.setMessage("row_hash mismatch");
                return vo;
            }
            expectedPrev = row.getRowHash();
        }
        vo.setMessage(vo.getCheckedCount() == 0 ? "no hashed rows" : "ok");
        return vo;
    }
}

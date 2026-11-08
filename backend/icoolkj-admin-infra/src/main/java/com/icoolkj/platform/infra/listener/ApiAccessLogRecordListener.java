package com.icoolkj.platform.infra.listener;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.model.log.ApiAccessLogRecord;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.audit.AuditIntegrityService;
import com.icoolkj.platform.infra.domain.ApiAccessLogEntity;
import com.icoolkj.platform.infra.mapper.ApiAccessLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ApiAccessLogRecordListener {

    private static final Logger log = LoggerFactory.getLogger(ApiAccessLogRecordListener.class);

    private final ApiAccessLogMapper apiAccessLogMapper;
    private final AuditIntegrityService auditIntegrityService;

    public ApiAccessLogRecordListener(ApiAccessLogMapper apiAccessLogMapper,
                                      AuditIntegrityService auditIntegrityService) {
        this.apiAccessLogMapper = apiAccessLogMapper;
        this.auditIntegrityService = auditIntegrityService;
    }

    @EventListener
    public void onApiAccessLogRecord(ApiAccessLogRecord record) {
        try {
            ApiAccessLogEntity entity = toEntity(record);
            String payload = AuditIntegrityService.joinPayload(
                    entity.getId(), entity.getTenantId(), entity.getRequestUri(), entity.getRequestMethod(),
                    entity.getUserId(), entity.getResponseCode(), entity.getAccessedAt());
            AuditIntegrityService.ChainHashes hashes = auditIntegrityService.next(
                    AuditIntegrityService.CHAIN_API_ACCESS,
                    entity.getTenantId(),
                    payload,
                    () -> apiAccessLogMapper.selectLatestRowHash(entity.getTenantId()));
            entity.setPrevHash(hashes.prevHash());
            entity.setRowHash(hashes.rowHash());
            apiAccessLogMapper.insert(entity);
        } catch (Exception ex) {
            log.warn("Save API access log failed, traceId={}", record.getTraceId(), ex);
        }
    }

    private ApiAccessLogEntity toEntity(ApiAccessLogRecord record) {
        ApiAccessLogEntity entity = new ApiAccessLogEntity();
        entity.setId(IdGeneratorUtils.nextIdStr());
        entity.setTenantId(defaultTenantId(record.getTenantId()));
        entity.setTraceId(record.getTraceId());
        entity.setRequestUri(record.getRequestUri());
        entity.setRequestMethod(record.getRequestMethod());
        entity.setClientIp(record.getClientIp());
        entity.setUserId(record.getUserId());
        entity.setUsername(record.getUsername());
        entity.setRequestParam(record.getRequestParam());
        entity.setResponseCode(record.getResponseCode());
        entity.setResponseMessage(record.getResponseMessage());
        entity.setCostTime(record.getCostTime());
        entity.setAccessedAt(record.getAccessedAt());
        return entity;
    }

    private String defaultTenantId(String tenantId) {
        return StringUtils.hasText(tenantId) ? tenantId : TenantConstants.PLATFORM_TENANT_ID;
    }
}

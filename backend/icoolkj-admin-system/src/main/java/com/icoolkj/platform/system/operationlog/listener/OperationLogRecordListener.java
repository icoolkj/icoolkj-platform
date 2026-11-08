package com.icoolkj.platform.system.operationlog.listener;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.model.log.OperationLogRecord;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.audit.AuditIntegrityService;
import com.icoolkj.platform.system.operationlog.domain.OperationLogEntity;
import com.icoolkj.platform.system.operationlog.mapper.OperationLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class OperationLogRecordListener {

    private static final Logger log = LoggerFactory.getLogger(OperationLogRecordListener.class);

    private final OperationLogMapper operationLogMapper;
    private final AuditIntegrityService auditIntegrityService;

    public OperationLogRecordListener(OperationLogMapper operationLogMapper,
                                      AuditIntegrityService auditIntegrityService) {
        this.operationLogMapper = operationLogMapper;
        this.auditIntegrityService = auditIntegrityService;
    }

    @EventListener
    public void onOperationLogRecord(OperationLogRecord record) {
        try {
            OperationLogEntity entity = toEntity(record);
            String payload = AuditIntegrityService.joinPayload(
                    entity.getId(), entity.getTenantId(), entity.getModuleName(), entity.getBusinessType(),
                    entity.getOperatorId(), entity.getOperationUrl(), entity.getOperationStatus(),
                    entity.getOperationAt());
            AuditIntegrityService.ChainHashes hashes = auditIntegrityService.next(
                    AuditIntegrityService.CHAIN_OPERATION,
                    entity.getTenantId(),
                    payload,
                    () -> operationLogMapper.selectLatestRowHash(entity.getTenantId()));
            entity.setPrevHash(hashes.prevHash());
            entity.setRowHash(hashes.rowHash());
            operationLogMapper.insert(entity);
        } catch (Exception ex) {
            log.warn("Save operation log failed, traceId={}", record.getTraceId(), ex);
        }
    }

    private OperationLogEntity toEntity(OperationLogRecord record) {
        OperationLogEntity entity = new OperationLogEntity();
        entity.setId(IdGeneratorUtils.nextIdStr());
        entity.setTenantId(defaultTenantId(record.getTenantId()));
        entity.setTraceId(record.getTraceId());
        entity.setModuleName(record.getModuleName());
        entity.setBusinessType(record.getBusinessType());
        entity.setMethod(record.getMethod());
        entity.setRequestMethod(record.getRequestMethod());
        entity.setOperatorId(record.getOperatorId());
        entity.setOperatorName(record.getOperatorName());
        entity.setOperationUrl(record.getOperationUrl());
        entity.setOperationIp(record.getOperationIp());
        entity.setOperationLocation(record.getOperationLocation());
        entity.setRequestParam(record.getRequestParam());
        entity.setResponseResult(record.getResponseResult());
        entity.setOperationStatus(record.getOperationStatus());
        entity.setErrorMessage(record.getErrorMessage());
        entity.setCostTime(record.getCostTime());
        entity.setOperationAt(record.getOperationAt());
        return entity;
    }

    private String defaultTenantId(String tenantId) {
        return StringUtils.hasText(tenantId) ? tenantId : TenantConstants.PLATFORM_TENANT_ID;
    }
}

package com.icoolkj.platform.system.operationlog.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.audit.AuditIntegrityService;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.system.operationlog.convert.OperationLogConvert;
import com.icoolkj.platform.system.operationlog.domain.OperationLogEntity;
import com.icoolkj.platform.system.operationlog.dto.OperationLogPageQuery;
import com.icoolkj.platform.system.operationlog.mapper.OperationLogMapper;
import com.icoolkj.platform.system.operationlog.service.OperationLogService;
import com.icoolkj.platform.system.operationlog.vo.OperationLogVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/** 操作日志服务实现 */
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private final AuditIntegrityService auditIntegrityService;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper,
                                   AuditIntegrityService auditIntegrityService) {
        this.operationLogMapper = operationLogMapper;
        this.auditIntegrityService = auditIntegrityService;
    }

    @Override
    public PageResult<OperationLogVO> page(OperationLogPageQuery query) {
        TenantQueryScope.applyPageQuery(query);
        long total = operationLogMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<OperationLogEntity> list = operationLogMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        List<OperationLogVO> rows = list.stream().map(OperationLogConvert::toVo).toList();
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public OperationLogVO getById(String id) {
        OperationLogEntity entity = operationLogMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.operation_log");
        }
        TenantQueryScope.assertReadable(entity.getTenantId());
        return OperationLogConvert.toVo(entity);
    }

    @Override
    public int purgeBefore(LocalDateTime before) {
        return operationLogMapper.purgeBefore(before);
    }

    @Override
    public int archiveThenPurge(LocalDateTime before) {
        List<String> tenantIds = operationLogMapper.selectTenantIdsBefore(before);
        int archived = operationLogMapper.archiveBefore(before);
        if (archived > 0) {
            operationLogMapper.purgeBefore(before);
            for (String tenantId : tenantIds) {
                reanchorLiveChain(tenantId);
            }
        }
        return archived;
    }

    private void reanchorLiveChain(String tenantId) {
        List<OperationLogEntity> rows = operationLogMapper.selectHashedByTenantOrdered(tenantId);
        auditIntegrityService.reanchor(
                AuditIntegrityService.CHAIN_OPERATION,
                tenantId,
                rows,
                row -> AuditIntegrityService.joinPayload(
                        row.getId(), row.getTenantId(), row.getModuleName(), row.getBusinessType(),
                        row.getOperatorId(), row.getOperationUrl(), row.getOperationStatus(),
                        row.getOperationAt()),
                (row, hashes) -> operationLogMapper.updateHashes(row.getId(), hashes.prevHash(), hashes.rowHash()));
    }

    @Override
    public int purgeArchiveBefore(LocalDateTime before) {
        return operationLogMapper.purgeArchiveBefore(before);
    }
}

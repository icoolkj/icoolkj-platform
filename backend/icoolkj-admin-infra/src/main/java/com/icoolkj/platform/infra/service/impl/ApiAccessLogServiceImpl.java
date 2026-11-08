package com.icoolkj.platform.infra.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.audit.AuditIntegrityService;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.infra.convert.ApiAccessLogConvert;
import com.icoolkj.platform.infra.domain.ApiAccessLogEntity;
import com.icoolkj.platform.infra.dto.ApiAccessLogPageQuery;
import com.icoolkj.platform.infra.mapper.ApiAccessLogMapper;
import com.icoolkj.platform.infra.service.ApiAccessLogService;
import com.icoolkj.platform.infra.vo.ApiAccessLogVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/** API访问日志服务实现 */
@Service
public class ApiAccessLogServiceImpl implements ApiAccessLogService {

    private final ApiAccessLogMapper apiAccessLogMapper;
    private final AuditIntegrityService auditIntegrityService;

    public ApiAccessLogServiceImpl(ApiAccessLogMapper apiAccessLogMapper,
                                   AuditIntegrityService auditIntegrityService) {
        this.apiAccessLogMapper = apiAccessLogMapper;
        this.auditIntegrityService = auditIntegrityService;
    }

    @Override
    public PageResult<ApiAccessLogVO> page(ApiAccessLogPageQuery query) {
        TenantQueryScope.applyPageQuery(query);
        long total = apiAccessLogMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<ApiAccessLogEntity> list = apiAccessLogMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        List<ApiAccessLogVO> rows = list.stream().map(ApiAccessLogConvert::toVo).toList();
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public ApiAccessLogVO getById(String id) {
        ApiAccessLogEntity entity = apiAccessLogMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.api_access_log");
        }
        TenantQueryScope.assertReadable(entity.getTenantId());
        return ApiAccessLogConvert.toVo(entity);
    }

    @Override
    public int purgeBefore(LocalDateTime before) {
        return apiAccessLogMapper.purgeBefore(before);
    }

    @Override
    public int archiveThenPurge(LocalDateTime before) {
        List<String> tenantIds = apiAccessLogMapper.selectTenantIdsBefore(before);
        int archived = apiAccessLogMapper.archiveBefore(before);
        if (archived > 0) {
            apiAccessLogMapper.purgeBefore(before);
            for (String tenantId : tenantIds) {
                reanchorLiveChain(tenantId);
            }
        }
        return archived;
    }

    private void reanchorLiveChain(String tenantId) {
        List<ApiAccessLogEntity> rows = apiAccessLogMapper.selectHashedByTenantOrdered(tenantId);
        auditIntegrityService.reanchor(
                AuditIntegrityService.CHAIN_API_ACCESS,
                tenantId,
                rows,
                row -> AuditIntegrityService.joinPayload(
                        row.getId(), row.getTenantId(), row.getRequestUri(), row.getRequestMethod(),
                        row.getUserId(), row.getResponseCode(), row.getAccessedAt()),
                (row, hashes) -> apiAccessLogMapper.updateHashes(row.getId(), hashes.prevHash(), hashes.rowHash()));
    }

    @Override
    public int purgeArchiveBefore(LocalDateTime before) {
        return apiAccessLogMapper.purgeArchiveBefore(before);
    }

}

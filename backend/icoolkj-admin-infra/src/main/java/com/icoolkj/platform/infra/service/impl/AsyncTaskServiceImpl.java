package com.icoolkj.platform.infra.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.datascope.RowAccessSupport;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.infra.async.AsyncTaskStatus;
import com.icoolkj.platform.infra.convert.AsyncTaskConvert;
import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.dto.AsyncTaskCreateRequest;
import com.icoolkj.platform.infra.dto.AsyncTaskPageQuery;
import com.icoolkj.platform.infra.dto.AsyncTaskUpdateRequest;
import com.icoolkj.platform.infra.excel.ExcelImportAsyncConstants;
import com.icoolkj.platform.infra.mapper.AsyncTaskMapper;
import com.icoolkj.platform.infra.service.AsyncTaskService;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.infra.vo.AsyncTaskVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/** 异步任务服务实现 */
@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private final AsyncTaskMapper asyncTaskMapper;
    private final RowAccessSupport rowAccessSupport;
    private final InfraFileService infraFileService;

    public AsyncTaskServiceImpl(AsyncTaskMapper asyncTaskMapper,
                                RowAccessSupport rowAccessSupport,
                                InfraFileService infraFileService) {
        this.asyncTaskMapper = asyncTaskMapper;
        this.rowAccessSupport = rowAccessSupport;
        this.infraFileService = infraFileService;
    }

    @Override
    public PageResult<AsyncTaskVO> page(AsyncTaskPageQuery query) {
        TenantQueryScope.applyPageQuery(query);
        // Personal work queue: never list peers' tasks (delete would release their excel staging).
        query.setUserId(requireCurrentUserId());
        long total = asyncTaskMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<AsyncTaskEntity> list = asyncTaskMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        List<AsyncTaskVO> rows = list.stream().map(AsyncTaskConvert::toVo).toList();
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public AsyncTaskVO getById(String id) {
        return AsyncTaskConvert.toVo(requireOwnedTask(id));
    }

    @Override
    public AsyncTaskVO getMineById(String id) {
        return AsyncTaskConvert.toVo(requireOwnedTask(id));
    }

    @Override
    public String create(AsyncTaskCreateRequest request) {
        throw new BusinessException("error.async.manual_mutate_forbidden");
    }

    @Override
    public void update(AsyncTaskUpdateRequest request) {
        throw new BusinessException("error.async.manual_mutate_forbidden");
    }

    @Override
    public void deleteById(String id) {
        AsyncTaskEntity entity = requireOwnedTask(id);
        if (!AsyncTaskStatus.isTerminal(entity.getTaskStatus())) {
            throw new BusinessException("error.async.delete_not_terminal");
        }
        String tenantId = entity.getTenantId();
        String bizType = entity.getBizType();
        AuditFieldHelper.markDeleted(entity);
        asyncTaskMapper.updateById(entity);
        // Keep staging files while the task exists; release only when the owner deletes the task.
        if (ExcelImportAsyncConstants.isReservedBizType(bizType)) {
            infraFileService.releaseBizFiles(tenantId, bizType, id);
        }
    }

    private AsyncTaskEntity requireOwnedTask(String id) {
        AsyncTaskEntity entity = asyncTaskMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.async_task");
        }
        rowAccessSupport.assertTenant(entity.getTenantId());
        String currentUserId = requireCurrentUserId();
        if (!currentUserId.equals(entity.getUserId())) {
            // Same as mine API: do not leak existence to peers (incl. tenant_admin DataScope ALL).
            throw new BusinessException("error.entity.not_found", "entity.name.async_task");
        }
        return entity;
    }

    private static String requireCurrentUserId() {
        String userId = SecurityContextHelper.getUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("error.auth.unauthorized");
        }
        return userId.trim();
    }

}

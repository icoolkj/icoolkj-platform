package com.icoolkj.platform.system.notice.service.impl;

import com.icoolkj.platform.common.enums.StatusEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.infra.dto.InfraFileRefBinding;
import com.icoolkj.platform.infra.fileaccess.FileAccessPurpose;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.infra.util.InfraFileContentUtils;
import com.icoolkj.platform.infra.vo.InfraFileVO;
import com.icoolkj.platform.system.notice.constant.NoticeFileConstants;
import com.icoolkj.platform.system.notice.constant.NoticeReleaseStatus;
import com.icoolkj.platform.system.notice.convert.NoticeConvert;
import com.icoolkj.platform.system.notice.domain.NoticeEntity;
import com.icoolkj.platform.system.notice.dto.NoticeBatchDeleteRequest;
import com.icoolkj.platform.system.notice.dto.NoticeCreateRequest;
import com.icoolkj.platform.system.notice.dto.NoticePageQuery;
import com.icoolkj.platform.system.notice.dto.NoticePublishedQuery;
import com.icoolkj.platform.system.notice.dto.NoticeUpdateRequest;
import com.icoolkj.platform.system.notice.mapper.NoticeMapper;
import com.icoolkj.platform.system.notice.service.NoticeService;
import com.icoolkj.platform.system.notice.support.NoticeContentUtils;
import com.icoolkj.platform.system.notice.vo.NoticeApproveDetailVO;
import com.icoolkj.platform.system.notice.vo.NoticeListVO;
import com.icoolkj.platform.system.notice.vo.NoticePublishedVO;
import com.icoolkj.platform.system.notice.vo.NoticeVO;
import com.icoolkj.platform.workflow.constant.WorkflowBizApproveStatus;
import com.icoolkj.platform.system.notice.constant.NoticeWorkflowCancelReasons;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.dto.WorkflowStartRequest;
import com.icoolkj.platform.workflow.service.WorkflowInstanceService;
import com.icoolkj.platform.workflow.service.WorkflowModelRegistry;
import com.icoolkj.platform.workflow.service.WorkflowProcessService;
import com.icoolkj.platform.workflow.service.WorkflowTaskLookupService;
import com.icoolkj.platform.workflow.support.WorkflowBizHistorySupport;
import com.icoolkj.platform.workflow.support.WorkflowCrossTenantSupport;
import com.icoolkj.platform.workflow.support.WorkflowVersionConfigLookup;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 通知公告表服务实现 */
@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;
    private final InfraFileService infraFileService;
    private final WorkflowProcessService workflowProcessService;
    private final WorkflowModelRegistry workflowModelRegistry;
    private final WorkflowTaskLookupService workflowTaskLookupService;
    private final WorkflowInstanceService workflowInstanceService;
    private final WorkflowCrossTenantSupport workflowCrossTenantSupport;
    private final WorkflowVersionConfigLookup versionConfigLookup;
    private final WorkflowBizHistorySupport workflowBizHistorySupport;

    public NoticeServiceImpl(NoticeMapper noticeMapper,
                             InfraFileService infraFileService,
                             WorkflowProcessService workflowProcessService,
                             WorkflowModelRegistry workflowModelRegistry,
                             WorkflowTaskLookupService workflowTaskLookupService,
                             WorkflowInstanceService workflowInstanceService,
                             WorkflowCrossTenantSupport workflowCrossTenantSupport,
                             WorkflowVersionConfigLookup versionConfigLookup,
                             WorkflowBizHistorySupport workflowBizHistorySupport) {
        this.noticeMapper = noticeMapper;
        this.infraFileService = infraFileService;
        this.workflowProcessService = workflowProcessService;
        this.workflowModelRegistry = workflowModelRegistry;
        this.workflowTaskLookupService = workflowTaskLookupService;
        this.workflowInstanceService = workflowInstanceService;
        this.workflowCrossTenantSupport = workflowCrossTenantSupport;
        this.versionConfigLookup = versionConfigLookup;
        this.workflowBizHistorySupport = workflowBizHistorySupport;
    }

    @Override
    public PageResult<NoticeListVO> page(NoticePageQuery query) {
        TenantQueryScope.applyPageQuery(query);
        return TenantQueryScope.runForExplicitTenant(query, () -> {
            long total = noticeMapper.countByQuery(query);
            if (total == 0) {
                return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
            }
            List<NoticeEntity> list = noticeMapper.selectPage(query);
            if (CollectionUtils.isEmpty(list)) {
                return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
            }
            return PageResult.of(list.stream().map(this::toListVo).toList(), total, query.getPageNo(), query.getPageSize());
        });
    }

    @Override
    public NoticeVO getById(String id) {
        NoticeEntity entity = requireReadableNotice(id);
        return toVo(entity);
    }

    @Override
    public List<NoticePublishedVO> listPublished(NoticePublishedQuery query) {
        String tenantId = TenantQueryScope.requiredOperatorTenantId();
        int limit = query.getLimit() == null ? 10 : Math.min(query.getLimit(), 50);
        return TenantScopeRunner.runIgnoringTenant(() -> noticeMapper.selectPublished(
                        tenantId, query.getPublishLocation().trim(), limit).stream()
                .map(this::toPublishedVo)
                .toList());
    }

    @Override
    public NoticePublishedVO getPublishedById(String id) {
        String tenantId = TenantQueryScope.requiredOperatorTenantId();
        NoticeEntity entity = TenantScopeRunner.runIgnoringTenant(() ->
                noticeMapper.selectPublishedById(tenantId, id));
        if (entity == null || !StatusEnum.isNormal(entity.getStatus())
                || !WorkflowBizApproveStatus.APPROVED.equals(entity.getApproveStatus())
                || !NoticeReleaseStatus.PUBLISHED.equals(entity.getReleaseStatus())) {
            throw new BusinessException("error.entity.not_found", "entity.name.notice");
        }
        return toPublishedVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(NoticeCreateRequest request) {
        NoticeEntity entity = NoticeConvert.toEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        entity.setNoticeContent(NoticeContentUtils.sanitize(entity.getNoticeContent()));
        noticeMapper.insert(entity);
        syncNoticeFileRefs(entity);
        return String.valueOf(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(NoticeUpdateRequest request) {
        NoticeEntity entity = requireReadableNotice(request.getId());
        assertEditable(entity);
        NoticeConvert.copy(request, entity);
        entity.setNoticeContent(NoticeContentUtils.sanitize(entity.getNoticeContent()));
        if (WorkflowBizApproveStatus.REJECTED.equals(entity.getApproveStatus())
                || WorkflowBizApproveStatus.CANCELLED.equals(entity.getApproveStatus())) {
            entity.setApproveStatus(WorkflowBizApproveStatus.DRAFT);
            entity.setProcessInstanceId(null);
            entity.setProcessDefinitionKey(null);
            entity.setSubmittedAt(null);
            entity.setFinishedAt(null);
        }
        // returned：保留 processInstanceId，状态仍为 returned，待重提
        AuditFieldHelper.fillUpdate(entity);
        noticeMapper.updateById(entity);
        syncNoticeFileRefs(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        NoticeEntity entity = requireReadableNotice(id);
        assertEditable(entity);
        if (hasApprovalHistory(entity)) {
            throw new BusinessException("error.notice.history_locked");
        }
        releaseNoticeFileRefs(entity);
        AuditFieldHelper.markDeleted(entity);
        noticeMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(NoticeBatchDeleteRequest request) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            return;
        }
        for (String id : request.getIds()) {
            deleteById(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(String id) {
        NoticeEntity entity = requireReadableNotice(id);
        String status = entity.getApproveStatus();
        // 幂等：已关闭直接成功
        if (WorkflowBizApproveStatus.CLOSED.equals(status)) {
            return;
        }
        if (WorkflowBizApproveStatus.RUNNING.equals(status)) {
            throw new BusinessException("error.notice.running_locked");
        }
        if (!hasApprovalHistory(entity)) {
            throw new BusinessException("error.notice.approve_status_invalid");
        }
        if (WorkflowBizApproveStatus.RETURNED.equals(status) && StringUtils.hasText(entity.getProcessInstanceId())) {
            try {
                workflowProcessService.cancelProcess(entity.getProcessInstanceId(),
                        NoticeWorkflowCancelReasons.CLOSE_AFTER_RETURN);
            } catch (BusinessException ignored) {
                // 已结束则忽略
            }
        }
        entity.setApproveStatus(WorkflowBizApproveStatus.CLOSED);
        entity.setReleaseStatus(NoticeReleaseStatus.UNPUBLISHED);
        if (entity.getFinishedAt() == null) {
            entity.setFinishedAt(LocalDateTime.now());
        }
        AuditFieldHelper.fillUpdate(entity);
        noticeMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(String id) {
        NoticeEntity entity = requireReadableNotice(id);
        if (!WorkflowBizApproveStatus.APPROVED.equals(entity.getApproveStatus())) {
            throw new BusinessException("error.notice.release_requires_approved");
        }
        if (NoticeReleaseStatus.PUBLISHED.equals(entity.getReleaseStatus())) {
            throw new BusinessException("error.notice.already_published");
        }
        entity.setReleaseStatus(NoticeReleaseStatus.PUBLISHED);
        entity.setPublishedAt(LocalDateTime.now());
        AuditFieldHelper.fillUpdate(entity);
        noticeMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublish(String id) {
        NoticeEntity entity = requireReadableNotice(id);
        if (!WorkflowBizApproveStatus.APPROVED.equals(entity.getApproveStatus())) {
            throw new BusinessException("error.notice.release_requires_approved");
        }
        if (!NoticeReleaseStatus.PUBLISHED.equals(entity.getReleaseStatus())) {
            throw new BusinessException("error.notice.not_published");
        }
        entity.setReleaseStatus(NoticeReleaseStatus.UNPUBLISHED);
        AuditFieldHelper.fillUpdate(entity);
        noticeMapper.updateById(entity);
    }

    @Override
    public Resource loadContentFile(String fileId) {
        return infraFileService.loadDownloadResource(fileId, FileAccessPurpose.NOTICE_CONTENT);
    }

    @Override
    public String resolveContentFileMediaType(String fileId) {
        InfraFileVO file = infraFileService.getById(fileId, FileAccessPurpose.NOTICE_CONTENT);
        return file == null ? null : file.getContentType();
    }

    private NoticeEntity requireReadableNotice(String id) {
        NoticeEntity entity = noticeMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.notice");
        }
        TenantQueryScope.assertReadable(entity.getTenantId());
        return entity;
    }

    private void syncNoticeFileRefs(NoticeEntity entity) {
        if (!StringUtils.hasText(entity.getTenantId()) || !StringUtils.hasText(entity.getId())) {
            return;
        }
        List<InfraFileRefBinding> bindings = new ArrayList<>();
        if (StringUtils.hasText(entity.getCoverFileId())) {
            bindings.add(new InfraFileRefBinding(entity.getCoverFileId(), NoticeFileConstants.REF_COVER));
        }
        for (String fileId : InfraFileContentUtils.extractFileIds(entity.getNoticeContent())) {
            bindings.add(new InfraFileRefBinding(fileId, NoticeFileConstants.REF_CONTENT));
        }
        infraFileService.syncBizFileRefs(
                entity.getTenantId(), NoticeFileConstants.BIZ_TYPE, entity.getId(), bindings);
    }

    private void releaseNoticeFileRefs(NoticeEntity entity) {
        if (!StringUtils.hasText(entity.getTenantId()) || !StringUtils.hasText(entity.getId())) {
            return;
        }
        infraFileService.releaseBizFiles(
                entity.getTenantId(), NoticeFileConstants.BIZ_TYPE, entity.getId());
    }

    private NoticeListVO toListVo(NoticeEntity entity) {
        NoticeListVO vo = NoticeConvert.toListVo(entity);
        if (vo != null && StringUtils.hasText(entity.getCoverFileId())) {
            vo.setCoverPreviewUrl(infraFileService.resolveAccessibleUrl(
                    entity.getCoverFileId(), entity.getCoverPreviewUrl()));
        }
        if (vo != null) {
            vo.setHasApprovalHistory(hasApprovalHistory(entity));
            vo.setAllowWithdraw(resolveAllowWithdraw(entity));
        }
        return vo;
    }

    private boolean hasApprovalHistory(NoticeEntity entity) {
        return workflowBizHistorySupport.hasApprovalHistory(
                entity.getProcessInstanceId(), NoticeFileConstants.BIZ_TYPE, entity.getId());
    }

    private Boolean resolveAllowWithdraw(NoticeEntity entity) {
        if (entity == null || !WorkflowBizApproveStatus.RUNNING.equals(entity.getApproveStatus())) {
            return false;
        }
        if (!StringUtils.hasText(entity.getProcessInstanceId())) {
            return true;
        }
        var behavior = versionConfigLookup
                .resolveByProcessInstanceId(entity.getProcessInstanceId(), null)
                .behavior();
        return behavior == null || !Boolean.FALSE.equals(behavior.getAllowWithdraw());
    }

    private NoticeVO toVo(NoticeEntity entity) {
        NoticeVO vo = NoticeConvert.toVo(entity);
        vo.setNoticeContent(NoticeContentUtils.sanitize(vo.getNoticeContent()));
        if (StringUtils.hasText(entity.getCoverFileId())) {
            vo.setCoverPreviewUrl(infraFileService.resolveAccessibleUrl(
                    entity.getCoverFileId(), entity.getCoverPreviewUrl()));
        }
        return vo;
    }

    private NoticePublishedVO toPublishedVo(NoticeEntity entity) {
        NoticePublishedVO vo = NoticeConvert.toPublishedVo(entity);
        if (vo == null) {
            return null;
        }
        vo.setNoticeContent(NoticeContentUtils.sanitize(vo.getNoticeContent()));
        if (StringUtils.hasText(entity.getCoverFileId())) {
            vo.setCoverPreviewUrl(infraFileService.resolveAccessibleUrl(
                    entity.getCoverFileId(), entity.getCoverPreviewUrl()));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitApprove(String id) {
        NoticeEntity entity = requireReadableNotice(id);
        String approveStatus = entity.getApproveStatus();
        if (WorkflowBizApproveStatus.RETURNED.equals(approveStatus)) {
            if (!StringUtils.hasText(entity.getProcessInstanceId())) {
                throw new BusinessException("error.workflow.instance_not_found");
            }
            workflowProcessService.resubmitFromStarterEdit(entity.getProcessInstanceId());
            entity.setApproveStatus(WorkflowBizApproveStatus.RUNNING);
            entity.setStarterUserId(SecurityContextHelper.getUserId());
            entity.setSubmittedAt(LocalDateTime.now());
            entity.setFinishedAt(null);
            noticeMapper.updateById(entity);
            return entity.getProcessInstanceId();
        }
        if (WorkflowBizApproveStatus.CLOSED.equals(approveStatus)
                || !WorkflowBizApproveStatus.isResubmittable(approveStatus)) {
            throw new BusinessException("error.notice.approve_status_invalid");
        }

        WfModelEntity model = workflowModelRegistry.requireActiveByBizType(
                entity.getTenantId(), NoticeFileConstants.BIZ_TYPE);

        WorkflowStartRequest startRequest = new WorkflowStartRequest();
        startRequest.setModelId(model.getId());
        startRequest.setProcessDefinitionKey(model.getModelKey());
        startRequest.setBizType(NoticeFileConstants.BIZ_TYPE);
        startRequest.setBizId(entity.getId());
        startRequest.setBizTitle(entity.getNoticeTitle());
        startRequest.setTenantId(entity.getTenantId());

        String processInstanceId = workflowProcessService.startProcess(startRequest);

        entity.setApproveStatus(WorkflowBizApproveStatus.RUNNING);
        entity.setProcessInstanceId(processInstanceId);
        entity.setProcessDefinitionKey(model.getModelKey());
        entity.setStarterUserId(SecurityContextHelper.getUserId());
        entity.setSubmittedAt(LocalDateTime.now());
        entity.setFinishedAt(null);
        noticeMapper.updateById(entity);
        return processInstanceId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(String id) {
        NoticeEntity entity = requireReadableNotice(id);
        if (!WorkflowBizApproveStatus.RUNNING.equals(entity.getApproveStatus())) {
            throw new BusinessException("error.notice.approve_status_invalid");
        }
        if (!StringUtils.hasText(entity.getProcessInstanceId())) {
            throw new BusinessException("error.workflow.instance_not_found");
        }
        workflowProcessService.cancelProcess(entity.getProcessInstanceId(),
                NoticeWorkflowCancelReasons.WITHDRAW);
    }

    @Override
    public NoticeApproveDetailVO getApproveDetail(String id) {
        NoticeEntity entity = workflowCrossTenantSupport.runIgnoringBizTenant(() -> noticeMapper.selectById(id));
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.notice");
        }
        workflowCrossTenantSupport.assertCanApproveDetail(entity.getProcessInstanceId(), entity.getId());
        NoticeApproveDetailVO detail = new NoticeApproveDetailVO();
        detail.setNotice(toVo(entity));
        detail.setApproveStatus(entity.getApproveStatus());
        detail.setProcessInstanceId(entity.getProcessInstanceId());
        if (StringUtils.hasText(entity.getProcessInstanceId())) {
            detail.setInstance(workflowInstanceService.getByInstanceId(entity.getProcessInstanceId()));
            String taskId = workflowTaskLookupService.findActiveTaskId(
                    entity.getProcessInstanceId(), SecurityContextHelper.getUserId());
            detail.setTaskId(taskId);
        }
        return detail;
    }

    private void assertEditable(NoticeEntity entity) {
        String approveStatus = entity.getApproveStatus();
        if (WorkflowBizApproveStatus.RUNNING.equals(approveStatus)) {
            throw new BusinessException("error.notice.running_locked");
        }
        if (WorkflowBizApproveStatus.APPROVED.equals(approveStatus)) {
            throw new BusinessException("error.notice.approved_locked");
        }
        if (WorkflowBizApproveStatus.CLOSED.equals(approveStatus)) {
            throw new BusinessException("error.notice.closed_locked");
        }
    }
}

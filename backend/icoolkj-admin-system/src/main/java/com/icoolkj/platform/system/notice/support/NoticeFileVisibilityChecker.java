package com.icoolkj.platform.system.notice.support;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.security.PermissionAuthorizationService;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.infra.domain.InfraFileEntity;
import com.icoolkj.platform.infra.fileaccess.FileAccessCaller;
import com.icoolkj.platform.infra.fileaccess.FileAccessPurpose;
import com.icoolkj.platform.infra.fileaccess.FileAccessVisibilityChecker;
import com.icoolkj.platform.infra.mapper.InfraFileRefMapper;
import com.icoolkj.platform.system.notice.constant.NoticeFileConstants;
import com.icoolkj.platform.system.notice.mapper.NoticeMapper;
import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import com.icoolkj.platform.workflow.support.WorkflowAccessGuard;
import com.icoolkj.platform.workflow.support.WorkflowBizProcessLookup;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 公告文件可见性：列表权限 + 关联引用 / 审批参与者 / 已发布。
 */
@Component
public class NoticeFileVisibilityChecker implements FileAccessVisibilityChecker {

    private static final Set<String> NOTICE_FILE_BIZ_TYPES = Set.of(
            NoticeFileConstants.BIZ_TYPE,
            NoticeFileConstants.BIZ_TYPE_COVER,
            NoticeFileConstants.BIZ_TYPE_RICHTEXT);

    private final NoticeMapper noticeMapper;
    private final InfraFileRefMapper infraFileRefMapper;
    private final PermissionAuthorizationService authorizationService;
    private final WorkflowBizProcessLookup workflowBizProcessLookup;
    private final WorkflowAccessGuard workflowAccessGuard;

    public NoticeFileVisibilityChecker(NoticeMapper noticeMapper,
                                       InfraFileRefMapper infraFileRefMapper,
                                       PermissionAuthorizationService authorizationService,
                                       WorkflowBizProcessLookup workflowBizProcessLookup,
                                       WorkflowAccessGuard workflowAccessGuard) {
        this.noticeMapper = noticeMapper;
        this.infraFileRefMapper = infraFileRefMapper;
        this.authorizationService = authorizationService;
        this.workflowBizProcessLookup = workflowBizProcessLookup;
        this.workflowAccessGuard = workflowAccessGuard;
    }

    @Override
    public boolean supports(InfraFileEntity file, FileAccessCaller caller) {
        if (caller != null && caller.purpose() == FileAccessPurpose.NOTICE_CONTENT) {
            return true;
        }
        if (file == null || !StringUtils.hasText(file.getBizType())) {
            return false;
        }
        String bizType = file.getBizType().trim();
        return NOTICE_FILE_BIZ_TYPES.contains(bizType) || bizType.startsWith("notice_");
    }

    @Override
    public boolean isVisible(InfraFileEntity file, FileAccessCaller caller) {
        if (file == null || caller == null || !caller.authenticated()) {
            return false;
        }
        String operatorTenantId = resolveOperatorTenantId();
        if (authorizationService.hasPermission("system:notice:list")
                && isNoticeRelatedFile(operatorTenantId, file)) {
            return true;
        }
        if (canAccessAsNoticeWorkflowParticipant(file.getId())) {
            return true;
        }
        return isPublishedNoticeFile(operatorTenantId, file.getId());
    }

    private boolean canAccessAsNoticeWorkflowParticipant(String fileId) {
        if (!authorizationService.hasPermission("workflow:approve:notice:view")) {
            return false;
        }
        String userId = SecurityContextHelper.getUserId();
        if (!StringUtils.hasText(userId)) {
            return false;
        }
        List<String> instanceIds = TenantScopeRunner.runIgnoringTenant(() ->
                noticeMapper.selectApprovingProcessInstanceIdsByFileId(fileId));
        if (CollectionUtils.isEmpty(instanceIds)) {
            return false;
        }
        for (String instanceId : instanceIds) {
            WfBizProcessEntity bizProcess = workflowBizProcessLookup.findByProcessInstanceId(instanceId);
            if (bizProcess == null) {
                continue;
            }
            try {
                workflowAccessGuard.assertCanViewInstance(userId, bizProcess);
                return true;
            } catch (BusinessException ignored) {
                // try next instance
            }
        }
        return false;
    }

    private boolean isPublishedNoticeFile(String tenantId, String fileId) {
        return TenantScopeRunner.runIgnoringTenant(() ->
                noticeMapper.countPublishedFileUsage(tenantId, fileId) > 0);
    }

    private boolean isNoticeRelatedFile(String tenantId, InfraFileEntity file) {
        if (StringUtils.hasText(file.getBizType()) && NOTICE_FILE_BIZ_TYPES.contains(file.getBizType().trim())) {
            return true;
        }
        if (!infraFileRefMapper.selectActiveByFileId(tenantId, file.getId()).isEmpty()) {
            return true;
        }
        if (!TenantConstants.PLATFORM_TENANT_ID.equals(tenantId)
                && !infraFileRefMapper.selectActiveByFileId(TenantConstants.PLATFORM_TENANT_ID, file.getId()).isEmpty()) {
            return true;
        }
        return TenantScopeRunner.runIgnoringTenant(() -> noticeMapper.countByCoverFileId(file.getId()) > 0);
    }

    private String resolveOperatorTenantId() {
        if (TenantContextHolder.isPlatform()) {
            return TenantConstants.PLATFORM_TENANT_ID;
        }
        return TenantQueryScope.requiredOperatorTenantId();
    }
}

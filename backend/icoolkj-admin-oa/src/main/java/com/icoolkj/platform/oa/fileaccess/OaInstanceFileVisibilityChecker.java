package com.icoolkj.platform.oa.fileaccess;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.auth.DataPermissionService;
import com.icoolkj.platform.framework.datascope.DataScopeSpec;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.infra.domain.InfraFileEntity;
import com.icoolkj.platform.infra.domain.InfraFileRefEntity;
import com.icoolkj.platform.infra.fileaccess.FileAccessCaller;
import com.icoolkj.platform.infra.fileaccess.FileAccessVisibilityChecker;
import com.icoolkj.platform.infra.mapper.InfraFileRefMapper;
import com.icoolkj.platform.oa.constant.OaInstanceFileConstants;
import com.icoolkj.platform.oa.domain.OaInstanceEntity;
import com.icoolkj.platform.oa.mapper.OaInstanceMapper;
import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import com.icoolkj.platform.workflow.support.WorkflowAccessGuard;
import com.icoolkj.platform.workflow.support.WorkflowBizProcessLookup;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * OA 实例附件可见性：发起人、流程参与者（含历史办理人/监控）、或 DataScope 可读管理侧。
 * <p>与 {@code OaInstanceServiceImpl.getById} / 审批详情 ACL 对齐，避免「详情能看、附件下不了」或反向泄漏。
 */
@Component
public class OaInstanceFileVisibilityChecker implements FileAccessVisibilityChecker {

    private final OaInstanceMapper oaInstanceMapper;
    private final InfraFileRefMapper infraFileRefMapper;
    private final WorkflowBizProcessLookup workflowBizProcessLookup;
    private final WorkflowAccessGuard workflowAccessGuard;
    private final DataPermissionService dataPermissionService;

    public OaInstanceFileVisibilityChecker(OaInstanceMapper oaInstanceMapper,
                                           InfraFileRefMapper infraFileRefMapper,
                                           WorkflowBizProcessLookup workflowBizProcessLookup,
                                           WorkflowAccessGuard workflowAccessGuard,
                                           DataPermissionService dataPermissionService) {
        this.oaInstanceMapper = oaInstanceMapper;
        this.infraFileRefMapper = infraFileRefMapper;
        this.workflowBizProcessLookup = workflowBizProcessLookup;
        this.workflowAccessGuard = workflowAccessGuard;
        this.dataPermissionService = dataPermissionService;
    }

    @Override
    public boolean supports(InfraFileEntity file, FileAccessCaller caller) {
        if (file == null || !StringUtils.hasText(file.getBizType())) {
            return false;
        }
        return OaInstanceFileConstants.BIZ_TYPE.equals(file.getBizType().trim());
    }

    @Override
    public boolean isVisible(InfraFileEntity file, FileAccessCaller caller) {
        if (file == null || caller == null || !caller.authenticated()) {
            return false;
        }
        String userId = caller.userId();
        if (!StringUtils.hasText(userId)) {
            userId = SecurityContextHelper.getUserId();
        }
        if (!StringUtils.hasText(userId)) {
            return false;
        }
        OaInstanceEntity instance = resolveInstance(file);
        if (instance == null) {
            // 发起表单未落库前：附件 / 富文本内嵌图尚无 instance 引用，仅允许上传人预览下载
            return userId.trim().equals(trimToNull(file.getUserId()));
        }
        if (userId.trim().equals(trimToNull(instance.getStarterUserId()))) {
            return true;
        }
        if (canAccessAsWorkflowParticipant(userId.trim(), instance)) {
            return true;
        }
        return dataPermissionService.isReadable(
                DataScopeSpec.userAndDept(), instance.getUserId(), instance.getDeptId());
    }

    private OaInstanceEntity resolveInstance(InfraFileEntity file) {
        Set<String> candidateIds = new LinkedHashSet<>();
        if (StringUtils.hasText(file.getBizId())) {
            candidateIds.add(file.getBizId().trim());
        }
        String tenantId = file.getTenantId();
        if (StringUtils.hasText(tenantId) && StringUtils.hasText(file.getId())) {
            List<InfraFileRefEntity> refs = infraFileRefMapper.selectActiveByFileId(tenantId, file.getId());
            if (!CollectionUtils.isEmpty(refs)) {
                for (InfraFileRefEntity ref : refs) {
                    if (ref != null
                            && OaInstanceFileConstants.BIZ_TYPE.equals(trimToNull(ref.getBizType()))
                            && StringUtils.hasText(ref.getBizId())) {
                        candidateIds.add(ref.getBizId().trim());
                    }
                }
            }
        }
        if (candidateIds.isEmpty()) {
            return null;
        }
        return TenantScopeRunner.runIgnoringTenant(() -> {
            for (String id : candidateIds) {
                OaInstanceEntity entity = oaInstanceMapper.selectById(id);
                if (entity != null) {
                    return entity;
                }
            }
            return null;
        });
    }

    private boolean canAccessAsWorkflowParticipant(String userId, OaInstanceEntity instance) {
        WfBizProcessEntity bizProcess = null;
        if (StringUtils.hasText(instance.getProcessInstanceId())) {
            bizProcess = workflowBizProcessLookup.findByProcessInstanceId(instance.getProcessInstanceId().trim());
        }
        if (bizProcess == null
                && StringUtils.hasText(instance.getCategoryCode())
                && StringUtils.hasText(instance.getId())) {
            bizProcess = workflowBizProcessLookup.findLatestByBiz(
                    instance.getCategoryCode().trim(), instance.getId());
        }
        if (bizProcess == null) {
            return false;
        }
        try {
            workflowAccessGuard.assertCanViewInstance(userId, bizProcess);
            return true;
        } catch (BusinessException ignored) {
            return false;
        }
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

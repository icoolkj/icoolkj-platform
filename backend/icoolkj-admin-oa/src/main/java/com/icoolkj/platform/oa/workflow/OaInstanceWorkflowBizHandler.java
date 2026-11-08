package com.icoolkj.platform.oa.workflow;

import com.icoolkj.platform.oa.domain.OaInstanceEntity;
import com.icoolkj.platform.oa.mapper.OaInstanceMapper;
import com.icoolkj.platform.workflow.constant.WorkflowBizApproveStatus;
import com.icoolkj.platform.workflow.support.AbstractWorkflowBizHandler;
import com.icoolkj.platform.workflow.support.WorkflowCrossTenantSupport;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** OA 实例工作流回调：回写 approve_status / processInstanceId / finishedAt */
@Component
public class OaInstanceWorkflowBizHandler extends AbstractWorkflowBizHandler {

    private final OaInstanceMapper instanceMapper;

    public OaInstanceWorkflowBizHandler(OaInstanceMapper instanceMapper, WorkflowCrossTenantSupport crossTenant) {
        super(crossTenant);
        this.instanceMapper = instanceMapper;
    }

    @Override
    public boolean supports(String bizType) {
        return StringUtils.hasText(bizType) && bizType.startsWith("oa_");
    }

    @Override
    public void onProcessApproved(String bizId, String processInstanceId) {
        OaInstanceEntity entity = requireInstance(bizId);
        entity.setApproveStatus(WorkflowBizApproveStatus.APPROVED);
        entity.setProcessInstanceId(processInstanceId);
        entity.setFinishedAt(LocalDateTime.now());
        updateBizIgnoringTenant(() -> instanceMapper.updateApproveFields(entity));
    }

    @Override
    public void onProcessRejected(String bizId, String processInstanceId) {
        OaInstanceEntity entity = requireInstance(bizId);
        entity.setApproveStatus(WorkflowBizApproveStatus.REJECTED);
        entity.setProcessInstanceId(processInstanceId);
        entity.setFinishedAt(LocalDateTime.now());
        updateBizIgnoringTenant(() -> instanceMapper.updateApproveFields(entity));
    }

    @Override
    public void onProcessCancelled(String bizId, String processInstanceId) {
        OaInstanceEntity entity = requireInstance(bizId);
        entity.setApproveStatus(WorkflowBizApproveStatus.CANCELLED);
        if (StringUtils.hasText(processInstanceId)) {
            entity.setProcessInstanceId(processInstanceId);
        }
        entity.setFinishedAt(LocalDateTime.now());
        updateBizIgnoringTenant(() -> instanceMapper.updateApproveFields(entity));
    }

    @Override
    public void onProcessReturned(String bizId, String processInstanceId, String rejectMode) {
        OaInstanceEntity entity = requireInstance(bizId);
        if ("starterEdit".equals(rejectMode)) {
            entity.setApproveStatus(WorkflowBizApproveStatus.RETURNED);
        } else if (!WorkflowBizApproveStatus.RUNNING.equals(entity.getApproveStatus())) {
            entity.setApproveStatus(WorkflowBizApproveStatus.RUNNING);
        }
        if (StringUtils.hasText(processInstanceId)) {
            entity.setProcessInstanceId(processInstanceId);
        }
        entity.setFinishedAt(null);
        updateBizIgnoringTenant(() -> instanceMapper.updateApproveFields(entity));
    }

    @Override
    public String resolveApproveStatus(String bizId) {
        if (!StringUtils.hasText(bizId)) {
            return null;
        }
        OaInstanceEntity entity = instanceMapper.selectById(bizId);
        return entity == null ? null : entity.getApproveStatus();
    }

    private OaInstanceEntity requireInstance(String bizId) {
        return requireBiz(bizId, instanceMapper::selectById, "error.oa.instance_not_found");
    }
}

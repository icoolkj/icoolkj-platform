package com.icoolkj.platform.system.notice.workflow;

import com.icoolkj.platform.system.notice.constant.NoticeFileConstants;
import com.icoolkj.platform.system.notice.domain.NoticeEntity;
import com.icoolkj.platform.system.notice.mapper.NoticeMapper;
import com.icoolkj.platform.workflow.constant.WorkflowBizApproveStatus;
import com.icoolkj.platform.workflow.support.AbstractWorkflowBizHandler;
import com.icoolkj.platform.workflow.support.WorkflowCrossTenantSupport;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 公告流程终态回调：仅同步审批字段包（approve_status / processInstanceId / finishedAt） */
@Component
public class NoticeWorkflowBizHandler extends AbstractWorkflowBizHandler {

    private final NoticeMapper noticeMapper;

    public NoticeWorkflowBizHandler(NoticeMapper noticeMapper, WorkflowCrossTenantSupport crossTenant) {
        super(crossTenant);
        this.noticeMapper = noticeMapper;
    }

    @Override
    public boolean supports(String bizType) {
        return NoticeFileConstants.BIZ_TYPE.equals(bizType);
    }

    @Override
    public void onProcessApproved(String bizId, String processInstanceId) {
        NoticeEntity entity = requireNotice(bizId);
        entity.setApproveStatus(WorkflowBizApproveStatus.APPROVED);
        entity.setFinishedAt(LocalDateTime.now());
        entity.setProcessInstanceId(processInstanceId);
        updateBizIgnoringTenant(() -> noticeMapper.updateApproveFields(entity));
    }

    @Override
    public void onProcessRejected(String bizId, String processInstanceId) {
        NoticeEntity entity = requireNotice(bizId);
        entity.setApproveStatus(WorkflowBizApproveStatus.REJECTED);
        entity.setFinishedAt(LocalDateTime.now());
        entity.setProcessInstanceId(processInstanceId);
        updateBizIgnoringTenant(() -> noticeMapper.updateApproveFields(entity));
    }

    @Override
    public void onProcessCancelled(String bizId, String processInstanceId) {
        NoticeEntity entity = requireNotice(bizId);
        entity.setApproveStatus(WorkflowBizApproveStatus.CANCELLED);
        entity.setFinishedAt(LocalDateTime.now());
        if (StringUtils.hasText(processInstanceId)) {
            entity.setProcessInstanceId(processInstanceId);
        }
        updateBizIgnoringTenant(() -> noticeMapper.updateApproveFields(entity));
    }

    @Override
    public void onProcessReturned(String bizId, String processInstanceId, String rejectMode) {
        NoticeEntity entity = requireNotice(bizId);
        if ("starterEdit".equals(rejectMode)) {
            entity.setApproveStatus(WorkflowBizApproveStatus.RETURNED);
        } else if (!WorkflowBizApproveStatus.RUNNING.equals(entity.getApproveStatus())) {
            entity.setApproveStatus(WorkflowBizApproveStatus.RUNNING);
        }
        if (StringUtils.hasText(processInstanceId)) {
            entity.setProcessInstanceId(processInstanceId);
        }
        entity.setFinishedAt(null);
        updateBizIgnoringTenant(() -> noticeMapper.updateApproveFields(entity));
    }

    @Override
    public String resolveApproveStatus(String bizId) {
        if (!StringUtils.hasText(bizId)) {
            return null;
        }
        NoticeEntity entity = noticeMapper.selectById(bizId);
        return entity == null ? null : entity.getApproveStatus();
    }

    private NoticeEntity requireNotice(String bizId) {
        return requireBiz(bizId, noticeMapper::selectById, "error.notice.not_found");
    }
}

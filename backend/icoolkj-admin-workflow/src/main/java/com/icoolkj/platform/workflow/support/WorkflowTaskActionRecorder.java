package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.workflow.domain.WfTaskActionEntity;
import com.icoolkj.platform.workflow.mapper.WfTaskActionMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 写入 wf_task_action 审计 */
@Component
public class WorkflowTaskActionRecorder {

    private final WfTaskActionMapper wfTaskActionMapper;

    public WorkflowTaskActionRecorder(WfTaskActionMapper wfTaskActionMapper) {
        this.wfTaskActionMapper = wfTaskActionMapper;
    }

    public void record(String tenantId,
                       String processInstanceId,
                       String taskId,
                       String taskDefinitionKey,
                       String actionType,
                       String fromUserId,
                       String toUserId,
                       String comment,
                       String payloadJson) {
        WfTaskActionEntity row = new WfTaskActionEntity();
        row.setId(IdGeneratorUtils.nextIdStr());
        row.setTenantId(StringUtils.hasText(tenantId) ? tenantId : TenantConstants.PLATFORM_TENANT_ID);
        row.setProcessInstanceId(processInstanceId);
        row.setTaskId(taskId);
        row.setTaskDefinitionKey(taskDefinitionKey);
        row.setActionType(actionType);
        row.setFromUserId(fromUserId);
        row.setToUserId(toUserId);
        row.setComment(comment);
        row.setPayloadJson(payloadJson);
        AuditFieldHelper.fillInsert(row);
        wfTaskActionMapper.insert(row);
    }
}

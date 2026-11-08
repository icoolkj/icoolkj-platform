package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.workflow.constant.WfTaskActionTypes;
import com.icoolkj.platform.workflow.constant.WorkflowVariables;
import com.icoolkj.platform.workflow.domain.WfTaskActionEntity;
import com.icoolkj.platform.workflow.vo.WorkflowHistoricTaskVO;
import com.icoolkj.platform.workflow.vo.WorkflowTaskVO;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 顺序前/后加签交接不 complete 同一任务，中间办理人不会落 ACT_HI_TASKINST。
 * 把 {@code wf_task_action} 中 phase=handoff/return 的记录还原为审核轨迹与已办行。
 */
public final class WorkflowAddSignAuditSupport {

    public static final String SYNTHETIC_TASK_ID_PREFIX = "wfa:";

    private WorkflowAddSignAuditSupport() {
    }

    public static boolean isApprovalHandoffAction(WfTaskActionEntity action) {
        if (action == null || !StringUtils.hasText(action.getActionType())) {
            return false;
        }
        String type = action.getActionType();
        String payload = action.getPayloadJson() == null ? "" : action.getPayloadJson();
        if (WfTaskActionTypes.ADD_SIGN_AFTER.equals(type)) {
            return payloadContainsPhase(payload, WfTaskActionTypes.PHASE_HANDOFF);
        }
        if (WfTaskActionTypes.ADD_SIGN_BEFORE.equals(type)) {
            return payloadContainsPhase(payload, WfTaskActionTypes.PHASE_RETURN);
        }
        return false;
    }

    public static String syntheticTaskId(WfTaskActionEntity action) {
        return SYNTHETIC_TASK_ID_PREFIX + action.getId();
    }

    public static WorkflowHistoricTaskVO toTimelineTask(WfTaskActionEntity action,
                                                        String taskName,
                                                        Map<String, String> displayNames) {
        WorkflowHistoricTaskVO vo = new WorkflowHistoricTaskVO();
        vo.setTaskId(syntheticTaskId(action));
        vo.setTaskName(StringUtils.hasText(taskName) ? taskName : action.getTaskDefinitionKey());
        vo.setTaskDefinitionKey(action.getTaskDefinitionKey());
        vo.setNodeStatus("completed");
        vo.setAssigneeUserId(action.getFromUserId());
        vo.setAssigneeUserName(resolveName(displayNames, action.getFromUserId()));
        vo.setApproved(Boolean.TRUE);
        vo.setComment(action.getComment());
        vo.setStartTime(action.getCreatedAt());
        vo.setEndTime(action.getCreatedAt());
        vo.setProcessInstanceId(action.getProcessInstanceId());
        return vo;
    }

    public static WorkflowTaskVO toDoneTaskVo(WfTaskActionEntity action,
                                              String taskName,
                                              String processDefinitionKey,
                                              Map<String, Object> variables) {
        WorkflowTaskVO vo = new WorkflowTaskVO();
        vo.setTaskId(syntheticTaskId(action));
        vo.setTaskName(StringUtils.hasText(taskName) ? taskName : action.getTaskDefinitionKey());
        vo.setTaskDefinitionKey(action.getTaskDefinitionKey());
        vo.setProcessInstanceId(action.getProcessInstanceId());
        vo.setProcessDefinitionKey(processDefinitionKey);
        if (variables != null) {
            vo.setBizType(WorkflowMappingSupport.asString(variables.get(WorkflowVariables.BIZ_TYPE)));
            vo.setBizId(WorkflowMappingSupport.asString(variables.get(WorkflowVariables.BIZ_ID)));
            vo.setBizTitle(WorkflowMappingSupport.asString(variables.get(WorkflowVariables.BIZ_TITLE)));
            vo.setStarterUserId(WorkflowMappingSupport.asString(variables.get(WorkflowVariables.STARTER_USER_ID)));
            vo.setStarterUserName(WorkflowMappingSupport.asString(variables.get(WorkflowVariables.STARTER_USER_NAME)));
        }
        vo.setCreateTime(action.getCreatedAt());
        vo.setEndTime(action.getCreatedAt());
        vo.setApproveResult("pass");
        vo.setCcTask(Boolean.FALSE);
        return vo;
    }

    private static boolean payloadContainsPhase(String payload, String phase) {
        if (!StringUtils.hasText(payload) || !StringUtils.hasText(phase)) {
            return false;
        }
        return payload.contains("\"phase\":\"" + phase + "\"")
                || payload.contains("\"phase\": \"" + phase + "\"");
    }

    private static String resolveName(Map<String, String> displayNames, String userId) {
        if (StringUtils.hasText(userId) && displayNames != null && displayNames.containsKey(userId)) {
            return displayNames.get(userId);
        }
        return userId;
    }
}

package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.workflow.constant.WfTaskActionTypes;
import com.icoolkj.platform.workflow.constant.WorkflowCancelReasons;
import com.icoolkj.platform.workflow.domain.WfTaskActionEntity;
import com.icoolkj.platform.workflow.vo.WorkflowHistoricTaskVO;
import java.util.Map;
import java.util.Set;
import org.springframework.util.StringUtils;

/**
 * 将 {@code wf_task_action} 中影响办理权/路径/生命周期的动作投影到流转记录。
 * <p>催办（urge）仅通知、不改变任务态，默认不进时间线。
 * <p>系统撤回文案由前端 gen i18n 按 {@code reasonCode} 解析，不经 {@code sys_i18n_message}。
 */
public final class WorkflowTimelineActionSupport {

    private static final Set<String> TIMELINE_TYPES = Set.of(
            WfTaskActionTypes.TRANSFER,
            WfTaskActionTypes.DELEGATE,
            WfTaskActionTypes.JUMP,
            WfTaskActionTypes.REJECT_JUMP,
            WfTaskActionTypes.WITHDRAW,
            WfTaskActionTypes.TERMINATE,
            WfTaskActionTypes.TIMEOUT,
            WfTaskActionTypes.REMOVE_SIGN,
            WfTaskActionTypes.ADD_SIGN_BEFORE,
            WfTaskActionTypes.ADD_SIGN_AFTER);

    private WorkflowTimelineActionSupport() {
    }

    public static boolean isTimelineAction(WfTaskActionEntity action) {
        if (action == null || !StringUtils.hasText(action.getActionType())) {
            return false;
        }
        String type = action.getActionType().trim();
        if (WorkflowAddSignAuditSupport.isApprovalHandoffAction(action)) {
            return true;
        }
        if (WfTaskActionTypes.ADD_SIGN_BEFORE.equals(type) || WfTaskActionTypes.ADD_SIGN_AFTER.equals(type)) {
            // 加签发起本身也进时间线（非仅 handoff/return）
            return true;
        }
        return TIMELINE_TYPES.contains(type);
    }

    /**
     * 加签交接若已落 HI，由 HI 行表达，避免重复；转办/委派/撤回等始终展示。
     */
    public static boolean skipWhenHistoricExists(WfTaskActionEntity action) {
        return WorkflowAddSignAuditSupport.isApprovalHandoffAction(action);
    }

    public static WorkflowHistoricTaskVO toTimelineTask(WfTaskActionEntity action,
                                                        String taskName,
                                                        Map<String, String> displayNames) {
        WorkflowHistoricTaskVO vo = new WorkflowHistoricTaskVO();
        vo.setTaskId(WorkflowAddSignAuditSupport.syntheticTaskId(action));
        vo.setTaskDefinitionKey(action.getTaskDefinitionKey());
        vo.setActionType(resolveDisplayActionType(action));
        vo.setNodeStatus("completed");
        vo.setAssigneeUserId(action.getFromUserId());
        vo.setAssigneeUserName(resolveName(displayNames, action.getFromUserId()));
        vo.setToUserId(action.getToUserId());
        vo.setToUserName(resolveName(displayNames, action.getToUserId()));
        applyCommentAndReason(vo, action);
        vo.setStartTime(action.getCreatedAt());
        vo.setEndTime(action.getCreatedAt());
        vo.setProcessInstanceId(action.getProcessInstanceId());

        String type = vo.getActionType();
        if (WfTaskActionTypes.WITHDRAW.equals(type)) {
            vo.setTaskName(StringUtils.hasText(taskName) ? taskName : "withdraw");
            vo.setApproved(Boolean.FALSE);
            vo.setNodeStatus("cancelled");
        } else if (WfTaskActionTypes.TERMINATE.equals(type)) {
            vo.setTaskName(StringUtils.hasText(taskName) ? taskName : "terminate");
            vo.setApproved(Boolean.FALSE);
            vo.setNodeStatus("cancelled");
        } else if (WfTaskActionTypes.TRANSFER.equals(type)
                || WfTaskActionTypes.DELEGATE.equals(type)
                || WfTaskActionTypes.JUMP.equals(type)
                || WfTaskActionTypes.REJECT_JUMP.equals(type)
                || WfTaskActionTypes.TIMEOUT.equals(type)
                || WfTaskActionTypes.REMOVE_SIGN.equals(type)
                || WfTaskActionTypes.ADD_SIGN_BEFORE.equals(type)
                || WfTaskActionTypes.ADD_SIGN_AFTER.equals(type)) {
            vo.setTaskName(StringUtils.hasText(taskName) ? taskName : action.getTaskDefinitionKey());
            vo.setApproved(null);
        } else {
            // 加签交接归还等：按通过节点展示
            vo.setTaskName(StringUtils.hasText(taskName) ? taskName : action.getTaskDefinitionKey());
            vo.setApproved(Boolean.TRUE);
        }
        return vo;
    }

    private static String resolveDisplayActionType(WfTaskActionEntity action) {
        String type = action.getActionType() == null ? "" : action.getActionType().trim();
        String payload = action.getPayloadJson() == null ? "" : action.getPayloadJson();
        if (WfTaskActionTypes.DELEGATE.equals(type)
                && (payload.contains("\"phase\":\"" + WfTaskActionTypes.PHASE_RETURN + "\"")
                || payload.contains("\"phase\": \"" + WfTaskActionTypes.PHASE_RETURN + "\""))) {
            return "delegate_return";
        }
        if (WorkflowAddSignAuditSupport.isApprovalHandoffAction(action)) {
            if (WfTaskActionTypes.ADD_SIGN_AFTER.equals(type)) {
                return "add_sign_handoff";
            }
            if (WfTaskActionTypes.ADD_SIGN_BEFORE.equals(type)) {
                return "add_sign_return";
            }
        }
        return type;
    }

    /**
     * 系统原因码只填 {@code reasonCode}；自由意见填 {@code comment}。
     * 历史英文/消息码 comment 映射为 reasonCode，不回传硬编码文案。
     */
    static void applyCommentAndReason(WorkflowHistoricTaskVO vo, WfTaskActionEntity action) {
        if (action == null || vo == null) {
            return;
        }
        String code = extractReasonCode(action.getPayloadJson());
        if (!StringUtils.hasText(code)) {
            code = WorkflowCancelReasons.resolveCode(action.getComment());
        }
        if (StringUtils.hasText(code) && WorkflowCancelReasons.isKnownCode(code)) {
            vo.setReasonCode(code);
            // 仅当 comment 不是已知码/历史硬编码时保留（自由意见）
            if (StringUtils.hasText(action.getComment())
                    && WorkflowCancelReasons.resolveCode(action.getComment()) == null
                    && !WorkflowCancelReasons.isKnownCode(action.getComment())) {
                vo.setComment(action.getComment());
            }
            return;
        }
        vo.setComment(action.getComment());
    }

    private static String extractReasonCode(String payloadJson) {
        if (!StringUtils.hasText(payloadJson)) {
            return null;
        }
        String marker = "\"reasonCode\":\"";
        int i = payloadJson.indexOf(marker);
        if (i < 0) {
            return null;
        }
        int start = i + marker.length();
        int end = payloadJson.indexOf('"', start);
        if (end <= start) {
            return null;
        }
        return payloadJson.substring(start, end);
    }

    private static String resolveName(Map<String, String> displayNames, String userId) {
        if (StringUtils.hasText(userId) && displayNames != null && displayNames.containsKey(userId)) {
            return displayNames.get(userId);
        }
        return userId;
    }
}

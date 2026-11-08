package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.common.model.page.PageQuery;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.workflow.constant.WorkflowVariables;
import com.icoolkj.platform.workflow.vo.WorkflowInstanceVO;
import com.icoolkj.platform.workflow.vo.WorkflowTaskVO;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Flowable 对象与 VO 映射辅助 */
public final class WorkflowMappingSupport {

    private WorkflowMappingSupport() {
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static <T> PageResult<T> paginate(List<T> all, PageQuery query) {
        if (all == null || all.isEmpty()) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        long pageNo = query.getPageNo();
        long pageSize = query.getPageSize();
        int from = (int) ((pageNo - 1) * pageSize);
        if (from >= all.size()) {
            return PageResult.of(Collections.emptyList(), (long) all.size(), pageNo, pageSize);
        }
        int to = Math.min(from + (int) pageSize, all.size());
        return PageResult.of(all.subList(from, to), (long) all.size(), pageNo, pageSize);
    }

    public static WorkflowTaskVO toTaskVo(Task task, Map<String, Object> variables) {
        WorkflowTaskVO vo = new WorkflowTaskVO();
        vo.setTaskId(task.getId());
        vo.setTaskName(task.getName());
        vo.setTaskDefinitionKey(task.getTaskDefinitionKey());
        vo.setProcessInstanceId(task.getProcessInstanceId());
        vo.setProcessDefinitionKey(extractProcessKey(task.getProcessDefinitionId()));
        vo.setCreateTime(toLocalDateTime(task.getCreateTime()));
        vo.setDueDate(toLocalDateTime(task.getDueDate()));
        vo.setCcTask(WorkflowCcTaskSupport.isCcTask(task));
        fillBizFields(vo, variables);
        return vo;
    }

    public static WorkflowTaskVO toHistoricTaskVo(HistoricTaskInstance task, Map<String, Object> variables) {
        WorkflowTaskVO vo = new WorkflowTaskVO();
        vo.setTaskId(task.getId());
        vo.setTaskName(task.getName());
        vo.setTaskDefinitionKey(task.getTaskDefinitionKey());
        vo.setProcessInstanceId(task.getProcessInstanceId());
        vo.setProcessDefinitionKey(extractProcessKey(task.getProcessDefinitionId()));
        vo.setCreateTime(toLocalDateTime(task.getCreateTime()));
        vo.setDueDate(toLocalDateTime(task.getDueDate()));
        vo.setEndTime(toLocalDateTime(task.getEndTime()));
        vo.setCcTask(WorkflowCcTaskSupport.isCcTask(task));
        resolveApproveResult(vo, task, variables);
        fillBizFields(vo, variables);
        return vo;
    }

    private static void resolveApproveResult(WorkflowTaskVO vo, HistoricTaskInstance task, Map<String, Object> variables) {
        if (task.getEndTime() == null) {
            return;
        }
        // 抄送完成不继承流程级 approved，避免显示成「通过/驳回」
        if (WorkflowCcTaskSupport.isCcTask(task)) {
            return;
        }
        Object approved = null;
        if (task.getTaskLocalVariables() != null) {
            approved = task.getTaskLocalVariables().get(WorkflowVariables.APPROVED);
        }
        if (!(approved instanceof Boolean) && variables != null) {
            approved = variables.get(WorkflowVariables.APPROVED);
        }
        if (approved instanceof Boolean bool) {
            vo.setApproveResult(bool ? "pass" : "reject");
        }
    }

    public static WorkflowInstanceVO toInstanceVo(HistoricProcessInstance instance, Map<String, Object> variables) {
        WorkflowInstanceVO vo = new WorkflowInstanceVO();
        vo.setInstanceId(instance.getId());
        vo.setProcessDefinitionKey(extractProcessKey(instance.getProcessDefinitionId()));
        vo.setStarterUserId(instance.getStartUserId());
        vo.setStartTime(toLocalDateTime(instance.getStartTime()));
        vo.setEndTime(toLocalDateTime(instance.getEndTime()));
        vo.setStatus(instance.getEndTime() == null ? "running" : "ended");
        fillBizFields(vo, variables);
        if (StringUtils.hasText(asString(variables.get(WorkflowVariables.STARTER_USER_NAME)))) {
            vo.setStarterUserName(asString(variables.get(WorkflowVariables.STARTER_USER_NAME)));
        }
        return vo;
    }

    private static void fillBizFields(WorkflowTaskVO vo, Map<String, Object> variables) {
        if (variables == null) {
            return;
        }
        vo.setBizType(asString(variables.get(WorkflowVariables.BIZ_TYPE)));
        vo.setBizId(asString(variables.get(WorkflowVariables.BIZ_ID)));
        vo.setBizTitle(asString(variables.get(WorkflowVariables.BIZ_TITLE)));
        vo.setStarterUserId(asString(variables.get(WorkflowVariables.STARTER_USER_ID)));
        vo.setStarterUserName(asString(variables.get(WorkflowVariables.STARTER_USER_NAME)));
    }

    private static void fillBizFields(WorkflowInstanceVO vo, Map<String, Object> variables) {
        if (variables == null) {
            return;
        }
        vo.setBizType(asString(variables.get(WorkflowVariables.BIZ_TYPE)));
        vo.setBizId(asString(variables.get(WorkflowVariables.BIZ_ID)));
        vo.setBizTitle(asString(variables.get(WorkflowVariables.BIZ_TITLE)));
        if (!StringUtils.hasText(vo.getStarterUserId())) {
            vo.setStarterUserId(asString(variables.get(WorkflowVariables.STARTER_USER_ID)));
        }
        if (!StringUtils.hasText(vo.getStarterUserName())) {
            vo.setStarterUserName(asString(variables.get(WorkflowVariables.STARTER_USER_NAME)));
        }
    }

    public static String extractProcessKey(String processDefinitionId) {
        if (!StringUtils.hasText(processDefinitionId)) {
            return null;
        }
        int colon = processDefinitionId.indexOf(':');
        return colon > 0 ? processDefinitionId.substring(0, colon) : processDefinitionId;
    }

    public static Map<String, Object> toVariableMap(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }
        return new HashMap<>(source);
    }

    public static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 用户实际办理完成的历史任务（排除流程撤回/删除时未产生结论的被动结束）。
     * <p>加签交接 / 驳回跳转会通过 changeActivityState 结束旧任务并带 deleteReason，
     * 但仍写入了 task-local {@code approved}，应计为已办。
     * <p>流程实例后续被撤回（cancel）时，已产生通过/驳回结论的已办仍保留在「我已处理」
     * （公告多次提交/撤回后可看到多轮审批记录）。
     *
     * @param cancelledProcessInstanceIds 保留参数兼容调用方；不再按已取消流程过滤已办
     */
    public static boolean isUserHandledHistoricTask(HistoricTaskInstance task,
                                                    java.util.Set<String> cancelledProcessInstanceIds) {
        if (task == null || task.getEndTime() == null) {
            return false;
        }
        if (StringUtils.hasText(task.getDeleteReason())) {
            Object approved = task.getTaskLocalVariables() == null
                    ? null : task.getTaskLocalVariables().get(WorkflowVariables.APPROVED);
            if (!(approved instanceof Boolean)) {
                return false;
            }
        }
        return true;
    }

    public static boolean matchesTaskQuery(WorkflowTaskVO vo, String bizTitle, String bizType,
                                           String processDefinitionKey, String taskName) {
        if (StringUtils.hasText(bizType) && !bizType.equals(vo.getBizType())) {
            return false;
        }
        if (StringUtils.hasText(processDefinitionKey) && !processDefinitionKey.equals(vo.getProcessDefinitionKey())) {
            return false;
        }
        if (StringUtils.hasText(taskName) && (vo.getTaskName() == null || !vo.getTaskName().contains(taskName))) {
            return false;
        }
        if (StringUtils.hasText(bizTitle) && (vo.getBizTitle() == null || !vo.getBizTitle().contains(bizTitle))) {
            return false;
        }
        return true;
    }

    public static boolean matchesInstanceQuery(WorkflowInstanceVO vo, String bizTitle, String bizType, String status) {
        if (StringUtils.hasText(bizType) && !bizType.equals(vo.getBizType())) {
            return false;
        }
        if (StringUtils.hasText(status) && !status.equals(vo.getStatus())) {
            return false;
        }
        if (StringUtils.hasText(bizTitle) && (vo.getBizTitle() == null || !vo.getBizTitle().contains(bizTitle))) {
            return false;
        }
        return true;
    }
}

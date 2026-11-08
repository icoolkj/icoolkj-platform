package com.icoolkj.platform.workflow.design.bpmn;

import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import org.flowable.bpmn.model.BoundaryEvent;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.MultiInstanceLoopCharacteristics;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.ThrowEvent;
import org.flowable.bpmn.model.TimerEventDefinition;
import org.flowable.bpmn.model.UserTask;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ApproveBpmnNodeEmitter implements BpmnNodeEmitter {

    @Override
    public String nodeType() {
        return LinearBpmnBuilder.NODE_APPROVE;
    }

    @Override
    public void appendElements(Process process, LinearBpmnBuilder.DesignStep step, BpmnModelEmitContext context) {
        String key = step.taskDefinitionKey();
        String displayName = StringUtils.hasText(step.name()) ? step.name() : key;
        boolean multi = step.isMultiInstance();

        UserTask task = new UserTask();
        task.setId(key);
        task.setName(displayName);
        if (multi) {
            task.setAssignee(BpmnElementIds.varEl(step.variableName()));
            task.getExecutionListeners().add(
                    BpmnModelElements.executionStartListener(BpmnElementIds.assigneeExecutionListenerEl()));
            MultiInstanceLoopCharacteristics loop = new MultiInstanceLoopCharacteristics();
            String mode = step.normalizedPassMode();
            loop.setSequential(LinearBpmnBuilder.PASS_SEQUENTIAL_ALL.equals(mode));
            loop.setInputDataItem(BpmnElementIds.varEl(BpmnElementIds.assigneeListVar(key)));
            loop.setElementVariable(step.variableName());
            loop.setCompletionCondition(LinearBpmnBuilder.PASS_ANY.equals(mode)
                    ? BpmnElementIds.multiInstancePassAnyEl()
                    : BpmnElementIds.multiInstancePassAllEl());
            task.setLoopCharacteristics(loop);
        } else {
            // 与会签一致：先用 execution start 写入办理人变量，再靠 assignee 表达式写入 RU+HI
            // （仅 TaskListener 里 DelegateTask#setAssignee 不会落历史办理人）
            if (StringUtils.hasText(step.variableName())) {
                task.setAssignee(BpmnElementIds.varEl(step.variableName()));
            }
            task.getExecutionListeners().add(
                    BpmnModelElements.executionStartListener(BpmnElementIds.assigneeExecutionListenerEl()));
            task.getTaskListeners().add(
                    BpmnModelElements.taskCreateListener(BpmnElementIds.assigneeTaskListenerEl()));
        }
        process.addFlowElement(task);

        if (StringUtils.hasText(step.dueDuration())) {
            appendTimeoutBoundary(process, key, step.dueDuration().trim(), step.timeoutAction());
        }

        ExclusiveGateway gateway = new ExclusiveGateway();
        gateway.setId(BpmnElementIds.resultGatewayId(key));
        gateway.setName(BpmnTechnicalNames.resultGateway(displayName));
        gateway.setDefaultFlow(BpmnElementIds.flowReject(key));
        process.addFlowElement(gateway);
    }

    private void appendTimeoutBoundary(Process process, String taskKey, String dueDuration, String timeoutAction) {
        String action = timeoutAction == null ? "" : timeoutAction.trim();
        boolean rejectEnd = "rejectEnd".equalsIgnoreCase(action);
        boolean autoApprove = "autoApprove".equalsIgnoreCase(action);

        BoundaryEvent boundary = new BoundaryEvent();
        boundary.setId(BpmnElementIds.timeoutBoundaryId(taskKey));
        boundary.setAttachedToRefId(taskKey);
        FlowElement attached = process.getFlowElement(taskKey);
        if (attached instanceof UserTask userTask) {
            boundary.setAttachedToRef(userTask);
        }
        // rejectEnd / autoApprove：中断任务；notify：非中断仅通知
        boundary.setCancelActivity(rejectEnd || autoApprove);
        TimerEventDefinition timer = new TimerEventDefinition();
        timer.setTimeDuration(dueDuration);
        boundary.addEventDefinition(timer);
        boundary.getExecutionListeners().add(
                BpmnModelElements.executionEndListener(BpmnElementIds.timeoutListenerEl()));
        if (autoApprove) {
            boundary.getExecutionListeners().add(
                    BpmnModelElements.executionEndListener(BpmnElementIds.timeoutAutoApproveListenerEl()));
        }
        process.addFlowElement(boundary);
        if (rejectEnd) {
            process.addFlowElement(BpmnModelElements.flow(
                    BpmnElementIds.timeoutBoundaryFlowId(taskKey),
                    boundary.getId(),
                    BpmnElementIds.END_REJECTED));
            return;
        }
        if (autoApprove) {
            // 出线在 appendFlows 接到 nextTarget（需知道下一节点）
            return;
        }
        // 非中断：连到无副作用 none throw（无 EventDefinition），仅触发 Listener 通知
        ThrowEvent sink = new ThrowEvent();
        sink.setId(BpmnElementIds.timeoutNotifySinkId(taskKey));
        process.addFlowElement(sink);
        process.addFlowElement(BpmnModelElements.flow(
                BpmnElementIds.timeoutBoundaryFlowId(taskKey),
                boundary.getId(),
                sink.getId()));
    }

    @Override
    public void appendFlows(Process process, LinearBpmnBuilder.DesignStep step, String nextTarget,
                            BpmnModelEmitContext context) {
        String key = step.taskDefinitionKey();
        String gwId = BpmnElementIds.resultGatewayId(key);
        process.addFlowElement(BpmnModelElements.flow(BpmnElementIds.flowToResultGateway(key), key, gwId));
        process.addFlowElement(BpmnModelElements.flowWithCondition(
                BpmnElementIds.flowApprove(key), gwId, nextTarget, BpmnElementIds.approvedConditionEl()));
        process.addFlowElement(BpmnModelElements.flow(
                BpmnElementIds.flowReject(key), gwId, BpmnElementIds.END_REJECTED));

        // 超时自动通过：中断后直达下一节点（已由 autoApprove listener 写入 approved=true）
        if (StringUtils.hasText(step.dueDuration())
                && "autoApprove".equalsIgnoreCase(step.timeoutAction() == null ? "" : step.timeoutAction().trim())) {
            process.addFlowElement(BpmnModelElements.flow(
                    BpmnElementIds.timeoutBoundaryFlowId(key),
                    BpmnElementIds.timeoutBoundaryId(key),
                    nextTarget));
        }
    }

    @Override
    public String entryId(LinearBpmnBuilder.DesignStep step) {
        return step.taskDefinitionKey();
    }
}

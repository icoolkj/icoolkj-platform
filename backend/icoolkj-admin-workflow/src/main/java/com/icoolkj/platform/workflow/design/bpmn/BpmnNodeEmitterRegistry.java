package com.icoolkj.platform.workflow.design.bpmn;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.design.DesignNodeHandlerRegistry;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.TerminateEventDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * BPMN 编译入口：DesignStep → Flowable {@link BpmnModel} → XML。
 * 每次 compile 使用独立 Session，避免单例可变状态。
 */
@Component
public class BpmnNodeEmitterRegistry {

    private final Map<String, BpmnNodeEmitter> emittersByType;

    public BpmnNodeEmitterRegistry(List<BpmnNodeEmitter> emitters) {
        Map<String, BpmnNodeEmitter> map = new LinkedHashMap<>();
        if (emitters != null) {
            for (BpmnNodeEmitter emitter : emitters) {
                map.put(DesignNodeHandlerRegistry.normalize(emitter.nodeType()), emitter);
            }
        }
        this.emittersByType = Map.copyOf(map);
    }

    public BpmnNodeEmitter require(String nodeType) {
        BpmnNodeEmitter emitter = emittersByType.get(DesignNodeHandlerRegistry.normalize(nodeType));
        if (emitter == null) {
            throw new BusinessException("error.workflow.design_task_key_invalid", nodeType);
        }
        return emitter;
    }

    public String compile(String modelKey, String processName, List<LinearBpmnBuilder.DesignStep> steps) {
        BpmnModel model = buildModel(modelKey, processName, steps);
        byte[] xml = new BpmnXMLConverter().convertToXML(model);
        return new String(xml, StandardCharsets.UTF_8);
    }

    public BpmnModel buildModel(String modelKey, String processName, List<LinearBpmnBuilder.DesignStep> steps) {
        if (!StringUtils.hasText(modelKey) || steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("modelKey and steps required");
        }
        return new CompileSession(this).build(modelKey, processName, steps);
    }

    /** 单次编译会话（线程隔离） */
    private static final class CompileSession implements BpmnModelEmitContext {

        private final BpmnNodeEmitterRegistry registry;
        private Process process;

        private CompileSession(BpmnNodeEmitterRegistry registry) {
            this.registry = registry;
        }

        private BpmnModel build(String modelKey, String processName, List<LinearBpmnBuilder.DesignStep> steps) {
            String name = StringUtils.hasText(processName) ? processName : modelKey;
            BpmnModel model = new BpmnModel();
            Process proc = new Process();
            proc.setId(modelKey);
            proc.setName(name);
            proc.setExecutable(true);
            model.addProcess(proc);
            this.process = proc;

            StartEvent start = new StartEvent();
            start.setId(BpmnElementIds.START);
            start.setName(BpmnTechnicalNames.START);
            proc.addFlowElement(start);

            for (LinearBpmnBuilder.DesignStep step : steps) {
                appendElements(step);
            }

            EndEvent endApproved = new EndEvent();
            endApproved.setId(BpmnElementIds.END_APPROVED);
            endApproved.setName(BpmnTechnicalNames.END_APPROVED);
            proc.addFlowElement(endApproved);

            // 终止结束：包容/并行任一分支驳回时杀掉全部并发令牌，避免兄弟分支汇合后仍走到 endApproved
            EndEvent endRejected = new EndEvent();
            endRejected.setId(BpmnElementIds.END_REJECTED);
            endRejected.setName(BpmnTechnicalNames.END_REJECTED);
            TerminateEventDefinition terminate = new TerminateEventDefinition();
            terminate.setTerminateAll(true);
            endRejected.addEventDefinition(terminate);
            proc.addFlowElement(endRejected);

            proc.addFlowElement(BpmnModelElements.flow(
                    BpmnElementIds.FLOW_START, BpmnElementIds.START, entryId(steps.get(0))));

            for (int i = 0; i < steps.size(); i++) {
                LinearBpmnBuilder.DesignStep step = steps.get(i);
                String nextTarget = (i + 1 < steps.size())
                        ? entryId(steps.get(i + 1))
                        : BpmnElementIds.END_APPROVED;
                appendFlows(step, nextTarget);
            }
            return model;
        }

        @Override
        public Process process() {
            return process;
        }

        @Override
        public BpmnNodeEmitter require(String nodeType) {
            return registry.require(nodeType);
        }

        @Override
        public void appendElements(LinearBpmnBuilder.DesignStep step) {
            require(step.normalizedType()).appendElements(process, step, this);
        }

        @Override
        public void appendFlows(LinearBpmnBuilder.DesignStep step, String nextTarget) {
            require(step.normalizedType()).appendFlows(process, step, nextTarget, this);
        }

        @Override
        public String entryId(LinearBpmnBuilder.DesignStep step) {
            return require(step.normalizedType()).entryId(step);
        }
    }
}

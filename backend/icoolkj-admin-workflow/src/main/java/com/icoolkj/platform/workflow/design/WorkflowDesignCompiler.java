package com.icoolkj.platform.workflow.design;

import com.icoolkj.platform.workflow.design.bpmn.BpmnNodeEmitterRegistry;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import com.icoolkj.platform.workflow.support.WorkflowDesignSoTSupport;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Design 层编译门面：校验 → DesignStep → BPMN + design_json 快照。
 */
@Component
public class WorkflowDesignCompiler {

    private final DesignNodeHandlerRegistry handlerRegistry;
    private final DesignValidationContextFactory validationFactory;
    private final BpmnNodeEmitterRegistry bpmnEmitterRegistry;
    private final WorkflowDesignSoTSupport designSoTSupport;

    public WorkflowDesignCompiler(DesignNodeHandlerRegistry handlerRegistry,
                                  DesignValidationContextFactory validationFactory,
                                  BpmnNodeEmitterRegistry bpmnEmitterRegistry,
                                  WorkflowDesignSoTSupport designSoTSupport) {
        this.handlerRegistry = handlerRegistry;
        this.validationFactory = validationFactory;
        this.bpmnEmitterRegistry = bpmnEmitterRegistry;
        this.designSoTSupport = designSoTSupport;
    }

    public CompiledDesign compile(String modelKey, String modelName,
                                  List<WorkflowModelDesignNodeRequest> nodes) {
        DesignValidationContext ctx = validationFactory.root();
        handlerRegistry.validateTree(nodes, ctx);
        List<LinearBpmnBuilder.DesignStep> steps = handlerRegistry.toSteps(nodes);
        String bpmnXml = bpmnEmitterRegistry.compile(modelKey, modelName, steps);
        String designJson = designSoTSupport.toDesignDocumentJson(nodes);
        return new CompiledDesign(bpmnXml, designJson, steps);
    }

    public record CompiledDesign(String bpmnXml, String designJson,
                                 List<LinearBpmnBuilder.DesignStep> steps) {
    }
}

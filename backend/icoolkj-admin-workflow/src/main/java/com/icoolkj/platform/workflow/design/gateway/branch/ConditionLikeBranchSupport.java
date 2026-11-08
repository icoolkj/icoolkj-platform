package com.icoolkj.platform.workflow.design.gateway.branch;

import com.icoolkj.platform.workflow.constant.DesignNodeTypes;
import com.icoolkj.platform.workflow.design.bpmn.BpmnTechnicalNames;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder.DesignBranch;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder.DesignStep;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 排他 / 包容网关：分支列表补齐（至少一条条件 + 默认出口） */
public final class ConditionLikeBranchSupport {

    private ConditionLikeBranchSupport() {
    }

    public static List<DesignBranch> resolveBranches(DesignStep step) {
        List<DesignBranch> raw = step.branchesOrEmpty();
        if (!CollectionUtils.isEmpty(raw)) {
            boolean hasDefault = false;
            boolean hasYes = false;
            List<DesignBranch> out = new ArrayList<>(raw.size());
            for (DesignBranch b : raw) {
                if (b.isDefault()) {
                    hasDefault = true;
                } else {
                    hasYes = true;
                }
                out.add(b);
            }
            if (!hasYes) {
                String expr = StringUtils.hasText(step.conditionExpression())
                        ? step.conditionExpression().trim() : "true";
                out.add(0, new DesignBranch(DesignNodeTypes.BRANCH_YES, BpmnTechnicalNames.BRANCH_YES, expr,
                        false, List.of()));
            }
            if (!hasDefault) {
                out.add(new DesignBranch(DesignNodeTypes.BRANCH_DEFAULT, BpmnTechnicalNames.BRANCH_DEFAULT, null,
                        true, List.of()));
            }
            return out;
        }
        String expr = StringUtils.hasText(step.conditionExpression())
                ? step.conditionExpression().trim() : "true";
        return List.of(
                new DesignBranch(DesignNodeTypes.BRANCH_YES, BpmnTechnicalNames.BRANCH_YES, expr, false, List.of()),
                new DesignBranch(DesignNodeTypes.BRANCH_DEFAULT, BpmnTechnicalNames.BRANCH_DEFAULT, null, true,
                        List.of()));
    }
}

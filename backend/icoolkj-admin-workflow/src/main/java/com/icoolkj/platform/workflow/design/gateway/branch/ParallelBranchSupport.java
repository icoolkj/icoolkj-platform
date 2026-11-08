package com.icoolkj.platform.workflow.design.gateway.branch;

import com.icoolkj.platform.workflow.design.bpmn.BpmnTechnicalNames;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder.DesignBranch;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder.DesignStep;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.CollectionUtils;

/** 并行网关：至少两路分支 */
public final class ParallelBranchSupport {

    private ParallelBranchSupport() {
    }

    public static List<DesignBranch> resolveBranches(DesignStep step) {
        List<DesignBranch> raw = step.branchesOrEmpty();
        if (CollectionUtils.isEmpty(raw) || raw.size() < 2) {
            List<DesignBranch> out = new ArrayList<>();
            if (!CollectionUtils.isEmpty(raw)) {
                out.addAll(raw);
            }
            while (out.size() < 2) {
                int n = out.size() + 1;
                out.add(new DesignBranch("p" + n, BpmnTechnicalNames.parallelBranch(n), null, false, List.of()));
            }
            return out;
        }
        return raw;
    }
}

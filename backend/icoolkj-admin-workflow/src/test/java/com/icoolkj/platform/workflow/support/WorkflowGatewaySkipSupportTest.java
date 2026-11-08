package com.icoolkj.platform.workflow.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.icoolkj.platform.workflow.constant.DesignNodeTypes;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignBranchRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.vo.WorkflowProgressNodeVO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WorkflowGatewaySkipSupportTest {

    @Test
    void exclusiveGatewaySkipsUnreachedSibling() {
        Map<String, WorkflowProgressNodeVO> byKey = new LinkedHashMap<>();
        byKey.put("a1", progress("a1", WorkflowGatewaySkipSupport.STATUS_COMPLETED));
        byKey.put("b1", progress("b1", WorkflowGatewaySkipSupport.STATUS_PENDING));

        WorkflowGatewaySkipSupport.markExclusiveUnreachedSkipped(List.of(conditionGateway()), byKey);

        assertEquals(WorkflowGatewaySkipSupport.STATUS_COMPLETED, byKey.get("a1").getNodeStatus());
        assertEquals(WorkflowGatewaySkipSupport.STATUS_SKIPPED, byKey.get("b1").getNodeStatus());
    }

    @Test
    void parallelGatewayDoesNotSkipSiblingWhilePending() {
        Map<String, WorkflowProgressNodeVO> byKey = new LinkedHashMap<>();
        byKey.put("a1", progress("a1", WorkflowGatewaySkipSupport.STATUS_ACTIVE));
        byKey.put("b1", progress("b1", WorkflowGatewaySkipSupport.STATUS_PENDING));

        WorkflowGatewaySkipSupport.markExclusiveUnreachedSkipped(List.of(parallelGateway()), byKey);

        assertEquals(WorkflowGatewaySkipSupport.STATUS_ACTIVE, byKey.get("a1").getNodeStatus());
        assertEquals(WorkflowGatewaySkipSupport.STATUS_PENDING, byKey.get("b1").getNodeStatus());
    }

    @Test
    void inclusiveGatewayDoesNotSkipSiblingWhilePending() {
        Map<String, WorkflowProgressNodeVO> byKey = new LinkedHashMap<>();
        byKey.put("a1", progress("a1", WorkflowGatewaySkipSupport.STATUS_COMPLETED));
        byKey.put("b1", progress("b1", WorkflowGatewaySkipSupport.STATUS_PENDING));

        WorkflowGatewaySkipSupport.markExclusiveUnreachedSkipped(List.of(inclusiveGateway()), byKey);

        assertEquals(WorkflowGatewaySkipSupport.STATUS_COMPLETED, byKey.get("a1").getNodeStatus());
        assertEquals(WorkflowGatewaySkipSupport.STATUS_PENDING, byKey.get("b1").getNodeStatus());
    }

    private static WorkflowProgressNodeVO progress(String key, String status) {
        WorkflowProgressNodeVO vo = new WorkflowProgressNodeVO();
        vo.setNodeKey(key);
        vo.setNodeStatus(status);
        return vo;
    }

    private static WorkflowModelDesignNodeRequest conditionGateway() {
        return gateway(DesignNodeTypes.CONDITION);
    }

    private static WorkflowModelDesignNodeRequest parallelGateway() {
        return gateway(DesignNodeTypes.PARALLEL);
    }

    private static WorkflowModelDesignNodeRequest inclusiveGateway() {
        return gateway(DesignNodeTypes.INCLUSIVE);
    }

    private static WorkflowModelDesignNodeRequest gateway(String type) {
        WorkflowModelDesignNodeRequest root = new WorkflowModelDesignNodeRequest();
        root.setNodeType(type);
        root.setTaskDefinitionKey("gw1");
        root.setBranches(List.of(branch("p1", "a1"), branch("p2", "b1")));
        return root;
    }

    private static WorkflowModelDesignBranchRequest branch(String id, String leafKey) {
        WorkflowModelDesignBranchRequest br = new WorkflowModelDesignBranchRequest();
        br.setId(id);
        WorkflowModelDesignNodeRequest leaf = new WorkflowModelDesignNodeRequest();
        leaf.setNodeType(DesignNodeTypes.APPROVE);
        leaf.setTaskDefinitionKey(leafKey);
        br.setNodes(List.of(leaf));
        return br;
    }
}

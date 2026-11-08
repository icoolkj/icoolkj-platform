package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.workflow.constant.DesignNodeTypes;
import com.icoolkj.platform.workflow.design.gateway.GatewayKindSupport;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignBranchRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.vo.WorkflowProgressNodeVO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 网关进度「未经过」标记：仅排他网关在兄弟路径已到达时跳过未走分支；
 * 并行/包容多路可同时有效，运行中不因兄弟到达而 skipped。
 */
public final class WorkflowGatewaySkipSupport {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_SKIPPED = "skipped";

    private WorkflowGatewaySkipSupport() {
    }

    public static void markExclusiveUnreachedSkipped(List<WorkflowModelDesignNodeRequest> designNodes,
                                                     Map<String, WorkflowProgressNodeVO> byKey) {
        if (CollectionUtils.isEmpty(designNodes) || byKey == null || byKey.isEmpty()) {
            return;
        }
        for (WorkflowModelDesignNodeRequest node : designNodes) {
            if (node == null) {
                continue;
            }
            String type = GatewayKindSupport.normalize(node.getNodeType());
            if (GatewayKindSupport.isAnyGateway(type)) {
                List<WorkflowModelDesignBranchRequest> branches = node.getBranches();
                if (!CollectionUtils.isEmpty(branches)) {
                    boolean exclusive = GatewayKindSupport.isExclusive(type);
                    boolean anyReached = false;
                    List<List<String>> branchLeafKeys = new ArrayList<>();
                    for (WorkflowModelDesignBranchRequest branch : branches) {
                        List<String> keys = collectLeafKeys(branch == null ? null : branch.getNodes());
                        branchLeafKeys.add(keys);
                        if (isAnyReached(keys, byKey)) {
                            anyReached = true;
                        }
                        markExclusiveUnreachedSkipped(branch == null ? null : branch.getNodes(), byKey);
                    }
                    if (exclusive && anyReached) {
                        for (List<String> keys : branchLeafKeys) {
                            if (!isAnyReached(keys, byKey)) {
                                for (String key : keys) {
                                    WorkflowProgressNodeVO progress = byKey.get(key);
                                    if (progress != null && STATUS_PENDING.equals(progress.getNodeStatus())) {
                                        progress.setNodeStatus(STATUS_SKIPPED);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static List<String> collectLeafKeys(List<WorkflowModelDesignNodeRequest> nodes) {
        List<String> keys = new ArrayList<>();
        if (CollectionUtils.isEmpty(nodes)) {
            return keys;
        }
        for (WorkflowModelDesignNodeRequest node : nodes) {
            if (node == null || !StringUtils.hasText(node.getTaskDefinitionKey())) {
                continue;
            }
            String type = GatewayKindSupport.normalize(node.getNodeType());
            if (GatewayKindSupport.isAnyGateway(type)) {
                if (node.getBranches() != null) {
                    for (WorkflowModelDesignBranchRequest branch : node.getBranches()) {
                        keys.addAll(collectLeafKeys(branch == null ? null : branch.getNodes()));
                    }
                }
            } else if (DesignNodeTypes.APPROVE.equals(type) || DesignNodeTypes.CC.equals(type)) {
                keys.add(node.getTaskDefinitionKey().trim());
            }
        }
        return keys;
    }

    static boolean isAnyReached(List<String> keys, Map<String, WorkflowProgressNodeVO> byKey) {
        for (String key : keys) {
            WorkflowProgressNodeVO node = byKey.get(key);
            if (node == null) {
                continue;
            }
            String status = node.getNodeStatus();
            if (STATUS_ACTIVE.equals(status) || STATUS_COMPLETED.equals(status) || STATUS_REJECTED.equals(status)) {
                return true;
            }
        }
        return false;
    }
}

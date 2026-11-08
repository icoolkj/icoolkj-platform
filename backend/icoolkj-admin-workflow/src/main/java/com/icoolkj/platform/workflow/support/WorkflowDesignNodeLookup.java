package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.workflow.domain.WfModelVersionEntity;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.mapper.WfModelVersionMapper;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 从实例对应版本 design_json 解析节点配置（运行中 + 已结束） */
@Component
public class WorkflowDesignNodeLookup {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final WfModelVersionMapper wfModelVersionMapper;
    private final WorkflowDesignSoTSupport designSoTSupport;

    public WorkflowDesignNodeLookup(RuntimeService runtimeService,
                                    HistoryService historyService,
                                    WfModelVersionMapper wfModelVersionMapper,
                                    WorkflowDesignSoTSupport designSoTSupport) {
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.wfModelVersionMapper = wfModelVersionMapper;
        this.designSoTSupport = designSoTSupport;
    }

    public WorkflowModelDesignNodeRequest findByProcessInstanceAndTaskKey(String processInstanceId,
                                                                          String taskDefinitionKey) {
        if (!StringUtils.hasText(processInstanceId) || !StringUtils.hasText(taskDefinitionKey)) {
            return null;
        }
        List<WorkflowModelDesignNodeRequest> nodes = loadDesignNodes(processInstanceId);
        if (CollectionUtils.isEmpty(nodes)) {
            return null;
        }
        return findInTree(nodes, taskDefinitionKey.trim());
    }

    /** 实例对应版本 design 中条件表达式引用的变量名（如 leaveDays） */
    public Set<String> collectConditionIdentifiers(String processInstanceId) {
        if (!StringUtils.hasText(processInstanceId)) {
            return Set.of();
        }
        return WorkflowConditionExpressionSupport.collectIdentifiers(loadDesignNodes(processInstanceId));
    }

    private List<WorkflowModelDesignNodeRequest> loadDesignNodes(String processInstanceId) {
        String pdId = resolveProcessDefinitionId(processInstanceId);
        if (!StringUtils.hasText(pdId)) {
            return List.of();
        }
        WfModelVersionEntity version = TenantScopeRunner.runIgnoringTenant(
                () -> wfModelVersionMapper.selectByProcessDefinitionId(pdId));
        if (version == null || !StringUtils.hasText(version.getDesignJson())) {
            return List.of();
        }
        return designSoTSupport.parseDesignNodes(version.getDesignJson());
    }

    private String resolveProcessDefinitionId(String processInstanceId) {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (pi != null && StringUtils.hasText(pi.getProcessDefinitionId())) {
            return pi.getProcessDefinitionId();
        }
        HistoricProcessInstance historic = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        return historic == null ? null : historic.getProcessDefinitionId();
    }

    private WorkflowModelDesignNodeRequest findInTree(List<WorkflowModelDesignNodeRequest> nodes, String key) {
        if (CollectionUtils.isEmpty(nodes)) {
            return null;
        }
        Deque<WorkflowModelDesignNodeRequest> q = new ArrayDeque<>(nodes);
        while (!q.isEmpty()) {
            WorkflowModelDesignNodeRequest n = q.poll();
            if (n == null) {
                continue;
            }
            if (key.equals(n.getTaskDefinitionKey())) {
                return n;
            }
            if (n.getBranches() != null) {
                for (var br : n.getBranches()) {
                    if (br != null && br.getNodes() != null) {
                        q.addAll(br.getNodes());
                    }
                }
            }
        }
        return null;
    }

    public static String rejectModeOf(WorkflowModelDesignNodeRequest node) {
        if (node == null || !StringUtils.hasText(node.getRejectMode())) {
            return "end";
        }
        String m = node.getRejectMode().trim().toLowerCase();
        if ("previous".equals(m) || "starter".equals(m) || "starteredit".equals(m)) {
            return "starteredit".equals(m) ? "starterEdit" : m;
        }
        return "end";
    }

    /**
     * 节点缺失时失败闭合（禁止转办），避免无 design_json / 键不匹配时默认放行。
     * 节点存在且未显式关闭时默认允许。
     */
    public static boolean allowTransfer(WorkflowModelDesignNodeRequest node) {
        if (node == null) {
            return false;
        }
        return node.getAllowTransfer() == null || Boolean.TRUE.equals(node.getAllowTransfer());
    }

    /** 同 {@link #allowTransfer}：节点缺失时禁止加签。 */
    public static boolean allowAddSign(WorkflowModelDesignNodeRequest node) {
        if (node == null) {
            return false;
        }
        return node.getAllowAddSign() == null || Boolean.TRUE.equals(node.getAllowAddSign());
    }

    /** 同 {@link #allowTransfer}：节点缺失时禁止委派。 */
    public static boolean allowDelegate(WorkflowModelDesignNodeRequest node) {
        if (node == null) {
            return false;
        }
        return node.getAllowDelegate() == null || Boolean.TRUE.equals(node.getAllowDelegate());
    }
}

package com.icoolkj.platform.workflow.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.design.gateway.GatewayKindSupport;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Design SoT 读写：
 * <ul>
 *   <li>{@code wf_model.process_config} — 当前草稿 / 最新全局属性 + design</li>
 *   <li>{@code wf_model_version.design_json} — 已发布版本不可变快照</li>
 * </ul>
 */
@Component
public class WorkflowDesignSoTSupport {

    public static final int DESIGN_SCHEMA_VERSION = 1;

    private final ObjectMapper objectMapper;
    private final WorkflowProcessConfigSupport processConfigSupport;

    public WorkflowDesignSoTSupport(ObjectMapper objectMapper,
                                    WorkflowProcessConfigSupport processConfigSupport) {
        this.objectMapper = objectMapper;
        this.processConfigSupport = processConfigSupport;
    }

    /** 版本快照：仅 design 文档（schemaVersion + nodes） */
    public String toDesignDocumentJson(List<WorkflowModelDesignNodeRequest> nodes) {
        Map<String, Object> design = buildDesignDocument(nodes);
        try {
            return objectMapper.writeValueAsString(design);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("error.workflow.process_config_invalid");
        }
    }

    /**
     * 以请求体 {@code nodes} 覆盖 {@code design}；保留 baseConfig 中的 startAcl/behavior/notify。
     */
    public String writeDesignAuthority(String baseConfigJson,
                                       List<WorkflowModelDesignNodeRequest> nodes) {
        WorkflowProcessConfigSupport.ProcessConfig config = processConfigSupport.parse(baseConfigJson);
        config.setDesign(buildDesignDocument(nodes));
        try {
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("error.workflow.process_config_invalid");
        }
    }

    public boolean hasDesignNodes(String designOrProcessConfigJson) {
        return StringUtils.hasText(extractDesignNodesJson(designOrProcessConfigJson));
    }

    /**
     * 优先解析独立 design_json；否则从 process_config.design / designNodes 提取。
     * 返回可被前端直接 JSON.parse 的 design 文档或 nodes 数组 JSON；无则 null。
     */
    public String resolveDesignPayload(String versionDesignJson, String processConfigJson) {
        if (StringUtils.hasText(versionDesignJson) && hasDesignNodes(versionDesignJson)) {
            return versionDesignJson.trim();
        }
        String fromConfig = extractDesignNodesJson(processConfigJson);
        return StringUtils.hasText(fromConfig) ? fromConfig : null;
    }

    /** 从 design 文档解析节点树（供编译 / 门禁 / 进度拓扑） */
    public List<WorkflowModelDesignNodeRequest> parseDesignNodes(String designOrProcessConfigJson) {
        String payload = resolveDesignPayload(designOrProcessConfigJson, null);
        if (!StringUtils.hasText(payload)) {
            return List.of();
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> root = objectMapper.readValue(payload, Map.class);
            Object nodes = root == null ? null : root.get("nodes");
            if (!(nodes instanceof List<?> list) || list.isEmpty()) {
                return List.of();
            }
            return objectMapper.convertValue(nodes,
                    objectMapper.getTypeFactory().constructCollectionType(
                            List.class, WorkflowModelDesignNodeRequest.class));
        } catch (Exception ex) {
            return List.of();
        }
    }

    /**
     * 收集 approve/cc 叶子 taskDefinitionKey（条件/并行网关本身不计入办理人门禁）。
     */
    public List<DesignLeafTask> collectApproverLeafTasks(String designOrProcessConfigJson) {
        List<DesignLeafTask> out = new ArrayList<>();
        collectApproverLeafTasks(parseDesignNodes(designOrProcessConfigJson), out);
        return out;
    }

    public record DesignLeafTask(String taskDefinitionKey, String name) {
    }

    private void collectApproverLeafTasks(List<WorkflowModelDesignNodeRequest> nodes, List<DesignLeafTask> out) {
        if (nodes == null) {
            return;
        }
        for (WorkflowModelDesignNodeRequest node : nodes) {
            if (node == null || !StringUtils.hasText(node.getTaskDefinitionKey())) {
                continue;
            }
            String type = GatewayKindSupport.normalize(node.getNodeType());
            if (GatewayKindSupport.isAnyGateway(type)) {
                if (node.getBranches() != null) {
                    for (var branch : node.getBranches()) {
                        if (branch != null) {
                            collectApproverLeafTasks(branch.getNodes(), out);
                        }
                    }
                }
                continue;
            }
            if ("approve".equals(type) || "cc".equals(type)) {
                String name = StringUtils.hasText(node.getName()) ? node.getName() : node.getTaskDefinitionKey();
                out.add(new DesignLeafTask(node.getTaskDefinitionKey().trim(), name));
            }
        }
    }

    private Map<String, Object> buildDesignDocument(List<WorkflowModelDesignNodeRequest> nodes) {
        Map<String, Object> design = new LinkedHashMap<>();
        design.put("schemaVersion", DESIGN_SCHEMA_VERSION);
        design.put("nodes", objectMapper.convertValue(nodes == null ? List.of() : nodes, List.class));
        return design;
    }

    private String extractDesignNodesJson(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> root = objectMapper.readValue(json, Map.class);
            if (root == null) {
                return null;
            }
            if (root.containsKey("nodes")) {
                Object nodes = root.get("nodes");
                if (nodes instanceof List<?> list && !list.isEmpty()) {
                    return objectMapper.writeValueAsString(root);
                }
            }
            Object design = root.get("design");
            if (design instanceof Map<?, ?> designMap) {
                Object nodes = designMap.get("nodes");
                if (nodes instanceof List<?> list && !list.isEmpty()) {
                    return objectMapper.writeValueAsString(designMap);
                }
            }
            Object designNodes = root.get("designNodes");
            if (designNodes instanceof List<?> list && !list.isEmpty()) {
                Map<String, Object> doc = new LinkedHashMap<>();
                doc.put("schemaVersion", DESIGN_SCHEMA_VERSION);
                doc.put("nodes", designNodes);
                return objectMapper.writeValueAsString(doc);
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }
}

package com.icoolkj.platform.api.ai.skill.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.icoolkj.platform.api.ai.skill.support.AiSkillArgumentSupport;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.system.ai.constant.AiSkillConstants;
import com.icoolkj.platform.workflow.dto.WorkflowInstancePageQuery;
import com.icoolkj.platform.workflow.service.WorkflowInstanceService;
import com.icoolkj.platform.workflow.vo.WorkflowInstanceVO;
import org.springframework.stereotype.Component;

@Component
public class ListMySubmittedAiSkillHandler implements AiSkillHandler {

    private final WorkflowInstanceService workflowInstanceService;
    private final ObjectMapper objectMapper;

    public ListMySubmittedAiSkillHandler(WorkflowInstanceService workflowInstanceService, ObjectMapper objectMapper) {
        this.workflowInstanceService = workflowInstanceService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String skillCode() {
        return AiSkillConstants.CODE_LIST_MY_SUBMITTED;
    }

    @Override
    public String execute(String argumentsJson) {
        int limit = AiSkillArgumentSupport.parseLimit(objectMapper, argumentsJson);
        WorkflowInstancePageQuery query = new WorkflowInstancePageQuery();
        query.setPageNo(1L);
        query.setPageSize((long) limit);
        PageResult<WorkflowInstanceVO> page = workflowInstanceService.pageSubmitted(query);

        ObjectNode root = objectMapper.createObjectNode();
        root.put("viewerUserId", SecurityContextHelper.getUserId());
        root.put("scope", "starter_self_only");
        root.put("total", page.getTotal() == null ? 0 : page.getTotal());
        ArrayNode rows = root.putArray("items");
        if (page.getList() != null) {
            for (WorkflowInstanceVO vo : page.getList()) {
                ObjectNode row = rows.addObject();
                row.put("instanceId", AiSkillArgumentSupport.nullToEmpty(vo.getInstanceId()));
                row.put("bizTitle", AiSkillArgumentSupport.nullToEmpty(vo.getBizTitle()));
                row.put("bizType", AiSkillArgumentSupport.nullToEmpty(vo.getBizType()));
                row.put("status", AiSkillArgumentSupport.nullToEmpty(vo.getStatus()));
                row.put("currentTaskName", AiSkillArgumentSupport.nullToEmpty(vo.getCurrentTaskName()));
                row.put("currentAssigneeUserNames", AiSkillArgumentSupport.nullToEmpty(vo.getCurrentAssigneeUserNames()));
                row.put("startTime", vo.getStartTime() == null ? "" : vo.getStartTime().toString());
                row.put("formRoute", AiSkillArgumentSupport.nullToEmpty(vo.getFormRoute()));
            }
        }
        return root.toString();
    }
}

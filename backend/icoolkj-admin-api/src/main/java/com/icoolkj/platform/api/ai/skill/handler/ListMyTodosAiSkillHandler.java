package com.icoolkj.platform.api.ai.skill.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.icoolkj.platform.api.ai.skill.support.AiSkillArgumentSupport;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.system.ai.constant.AiSkillConstants;
import com.icoolkj.platform.workflow.dto.WorkflowTaskPageQuery;
import com.icoolkj.platform.workflow.service.WorkflowTaskService;
import com.icoolkj.platform.workflow.vo.WorkflowTaskVO;
import org.springframework.stereotype.Component;

@Component
public class ListMyTodosAiSkillHandler implements AiSkillHandler {

    private final WorkflowTaskService workflowTaskService;
    private final ObjectMapper objectMapper;

    public ListMyTodosAiSkillHandler(WorkflowTaskService workflowTaskService, ObjectMapper objectMapper) {
        this.workflowTaskService = workflowTaskService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String skillCode() {
        return AiSkillConstants.CODE_LIST_MY_TODOS;
    }

    @Override
    public String execute(String argumentsJson) {
        int limit = AiSkillArgumentSupport.parseLimit(objectMapper, argumentsJson);
        WorkflowTaskPageQuery query = new WorkflowTaskPageQuery();
        query.setPageNo(1L);
        query.setPageSize((long) limit);
        PageResult<WorkflowTaskVO> page = workflowTaskService.pageTodo(query);

        ObjectNode root = objectMapper.createObjectNode();
        root.put("viewerUserId", SecurityContextHelper.getUserId());
        root.put("scope", "assignee_self_only");
        root.put("total", page.getTotal() == null ? 0 : page.getTotal());
        ArrayNode rows = root.putArray("items");
        if (page.getList() != null) {
            for (WorkflowTaskVO vo : page.getList()) {
                ObjectNode row = rows.addObject();
                row.put("taskId", AiSkillArgumentSupport.nullToEmpty(vo.getTaskId()));
                row.put("taskName", AiSkillArgumentSupport.nullToEmpty(vo.getTaskName()));
                row.put("bizTitle", AiSkillArgumentSupport.nullToEmpty(vo.getBizTitle()));
                row.put("bizType", AiSkillArgumentSupport.nullToEmpty(vo.getBizType()));
                row.put("starterUserName", AiSkillArgumentSupport.nullToEmpty(vo.getStarterUserName()));
                row.put("createTime", vo.getCreateTime() == null ? "" : vo.getCreateTime().toString());
                row.put("formRoute", AiSkillArgumentSupport.nullToEmpty(vo.getFormRoute()));
            }
        }
        return root.toString();
    }
}

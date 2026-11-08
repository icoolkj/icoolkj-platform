package com.icoolkj.platform.workflow.spi;

import java.util.List;

/** 解析发起人部门 / 角色 / 岗位（SecurityContext 无这些字段时由系统模块补齐） */
public interface WorkflowOperatorContextResolver {

    record OperatorContext(
            String userId,
            String deptId,
            List<String> roleCodes,
            List<String> postIds
    ) {
    }

    OperatorContext resolve(String userId);
}

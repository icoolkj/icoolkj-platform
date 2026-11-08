package com.icoolkj.platform.workflow.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.workflow.dto.WorkflowModelCreateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelDeployRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignDeployRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelPageQuery;
import com.icoolkj.platform.workflow.dto.WorkflowModelSyncRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelUpdateRequest;
import com.icoolkj.platform.workflow.vo.WorkflowModelDeployResultVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelDetailVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelSyncCatalogVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelVersionVO;
import java.util.List;

/** 流程模型服务 */
public interface WorkflowModelService {

    PageResult<WorkflowModelVO> page(WorkflowModelPageQuery query);

    WorkflowModelDetailVO getById(String id);

    List<WorkflowModelVersionVO> listVersions(String modelId);

    String create(WorkflowModelCreateRequest request);

    void update(WorkflowModelUpdateRequest request);

    WorkflowModelDeployResultVO deploy(WorkflowModelDeployRequest request);

    /** 竖向设计器：生成线性 BPMN 部署并同步本租户审批人规则 */
    WorkflowModelDeployResultVO designDeploy(WorkflowModelDesignDeployRequest request);

    /** 租户可同步的平台目录 */
    List<WorkflowModelSyncCatalogVO> listSyncCatalog();

    /** 从平台目录同步到当前租户（含示例 BPMN 首次部署与审批人规则复制） */
    String syncFromPlatform(WorkflowModelSyncRequest request);
}

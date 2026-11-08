package com.icoolkj.platform.workflow.engine;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.workflow.config.WorkflowTenantSupport;
import com.icoolkj.platform.workflow.vo.WorkflowModelDeployResultVO;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** Flowable 流程定义部署 */
@Service
public class FlowableProcessDeployService {

    private final RepositoryService repositoryService;

    public FlowableProcessDeployService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public ProcessDefinition getLatestByKey(String processDefinitionKey) {
        if (!StringUtils.hasText(processDefinitionKey)) {
            return null;
        }
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();
    }

    /** @param flowableTenantId {@code null} 表示查询无 tenant 的历史部署（Flowable 存为空串） */
    public ProcessDefinition getLatestByKey(String processDefinitionKey, String flowableTenantId) {
        if (!StringUtils.hasText(processDefinitionKey)) {
            return null;
        }
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .processDefinitionTenantId(flowableTenantId == null ? "" : normalizeTenant(flowableTenantId));
        return query.latestVersion().singleResult();
    }

    public boolean isDeployed(String processDefinitionKey) {
        return getLatestByKey(processDefinitionKey) != null;
    }

    public boolean isDeployed(String processDefinitionKey, String flowableTenantId) {
        return getLatestByKey(processDefinitionKey, flowableTenantId) != null;
    }

    /**
     * 解析流程启动应使用的 Flowable tenantId。
     *
     * @return 模型归属租户；{@code null} 表示匹配无 tenant 的历史部署（启动时不设置 tenantId）
     */
    public String resolveFlowableTenantId(String processDefinitionKey, String modelTenantId) {
        String normalizedModelTenant = WorkflowTenantSupport.flowableTenantIdForModel(modelTenantId);
        if (isDeployed(processDefinitionKey, normalizedModelTenant)) {
            return normalizedModelTenant;
        }
        if (isDeployed(processDefinitionKey, null)) {
            return null;
        }
        return normalizedModelTenant;
    }

    public boolean hasDefinition(String processDefinitionKey, String modelTenantId) {
        String normalizedModelTenant = WorkflowTenantSupport.flowableTenantIdForModel(modelTenantId);
        return isDeployed(processDefinitionKey, normalizedModelTenant) || isDeployed(processDefinitionKey, null);
    }

    public Deployment deployClasspathResource(String classpathResource, String deploymentName, String flowableTenantId) {
        return repositoryService.createDeployment()
                .name(deploymentName)
                .tenantId(normalizeTenant(flowableTenantId))
                .addClasspathResource(classpathResource)
                .deploy();
    }

    public WorkflowModelDeployResultVO deployBpmnXml(String resourceName,
                                                     String bpmnXml,
                                                     String deploymentName,
                                                     String flowableTenantId) {
        Deployment deployment = repositoryService.createDeployment()
                .name(deploymentName)
                .tenantId(normalizeTenant(flowableTenantId))
                .addString(resourceName, bpmnXml)
                .deploy();
        return toDeployResult(deployment, resourceName);
    }

    public WorkflowModelDeployResultVO deployClasspath(String classpathResource,
                                                       String deploymentName,
                                                       String flowableTenantId) {
        Deployment deployment = deployClasspathResource(classpathResource, deploymentName, flowableTenantId);
        String resourceName = extractFileName(classpathResource);
        return toDeployResult(deployment, resourceName);
    }

    private WorkflowModelDeployResultVO toDeployResult(Deployment deployment, String resourceName) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        WorkflowModelDeployResultVO result = new WorkflowModelDeployResultVO();
        if (definition != null) {
            result.setDeploymentId(deployment.getId());
            result.setProcessDefinitionId(definition.getId());
            result.setProcessDefinitionKey(definition.getKey());
            result.setVersionNo(definition.getVersion());
        }
        result.setModelId(resourceName);
        return result;
    }

    private String extractFileName(String classpathResource) {
        if (!StringUtils.hasText(classpathResource)) {
            return classpathResource;
        }
        int index = classpathResource.lastIndexOf('/');
        return index >= 0 ? classpathResource.substring(index + 1) : classpathResource;
    }

    private static String normalizeTenant(String flowableTenantId) {
        if (!StringUtils.hasText(flowableTenantId)) {
            return TenantConstants.PLATFORM_TENANT_ID;
        }
        return flowableTenantId.trim();
    }
}

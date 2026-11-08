package com.icoolkj.platform.workflow.config;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.workflow.engine.FlowableProcessDeployService;
import com.icoolkj.platform.workflow.vo.WorkflowModelDeployResultVO;
import org.flowable.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 应急：启动时部署 classpath BPMN（幂等：同 key 已存在则跳过）。
 * <p>默认关闭；日常种子走 DB {@code bpmn_xml}/{@code design_json} + {@link WfModelBootstrapSync}。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@EnableConfigurationProperties(WorkflowBootstrapProperties.class)
public class WorkflowProcessBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessBootstrap.class);

    private final WorkflowBootstrapProperties bootstrapProperties;
    private final FlowableProcessDeployService processDeployService;

    public WorkflowProcessBootstrap(WorkflowBootstrapProperties bootstrapProperties,
                                    FlowableProcessDeployService processDeployService) {
        this.bootstrapProperties = bootstrapProperties;
        this.processDeployService = processDeployService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!bootstrapProperties.isClasspathDeployActive()) {
            return;
        }
        if (CollectionUtils.isEmpty(bootstrapProperties.getClasspathResources())) {
            return;
        }
        for (String resource : bootstrapProperties.getClasspathResources()) {
            deployIfAbsent(resource);
        }
    }

    private void deployIfAbsent(String classpathResource) {
        if (!StringUtils.hasText(classpathResource)) {
            return;
        }
        String processKey = resolveProcessKey(classpathResource);
        if (StringUtils.hasText(processKey)
                && (processDeployService.isDeployed(processKey, TenantConstants.PLATFORM_TENANT_ID)
                || processDeployService.isDeployed(processKey))) {
            ProcessDefinition existing = processDeployService.getLatestByKey(processKey, TenantConstants.PLATFORM_TENANT_ID);
            if (existing == null) {
                existing = processDeployService.getLatestByKey(processKey);
            }
            log.info("Flowable process already deployed: key={}, version={}, deploymentId={}",
                    processKey,
                    existing == null ? null : existing.getVersion(),
                    existing == null ? null : existing.getDeploymentId());
            return;
        }
        WorkflowModelDeployResultVO result = processDeployService.deployClasspath(
                classpathResource,
                "bootstrap:" + extractFileName(classpathResource),
                TenantConstants.PLATFORM_TENANT_ID);
        log.info("Flowable process deployed from {}: key={}, version={}, deploymentId={}",
                classpathResource,
                result.getProcessDefinitionKey(),
                result.getVersionNo(),
                result.getDeploymentId());
    }

    private String resolveProcessKey(String classpathResource) {
        return extractFileName(classpathResource);
    }

    private String extractFileName(String classpathResource) {
        int index = classpathResource.lastIndexOf('/');
        String fileName = index >= 0 ? classpathResource.substring(index + 1) : classpathResource;
        if (fileName.endsWith(".bpmn20.xml")) {
            return fileName.substring(0, fileName.length() - ".bpmn20.xml".length());
        }
        if (fileName.endsWith(".bpmn")) {
            return fileName.substring(0, fileName.length() - ".bpmn".length());
        }
        return fileName;
    }
}

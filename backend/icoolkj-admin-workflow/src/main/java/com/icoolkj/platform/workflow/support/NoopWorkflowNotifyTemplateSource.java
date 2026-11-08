package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.workflow.spi.WorkflowNotifyTemplateSource;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/** 无 system 覆盖实现时的空模板源 */
@Component
@ConditionalOnMissingBean(WorkflowNotifyTemplateSource.class)
public class NoopWorkflowNotifyTemplateSource implements WorkflowNotifyTemplateSource {

    @Override
    public Optional<WorkflowNotifyTemplateSource.EventTemplate> find(String eventType) {
        return Optional.empty();
    }
}

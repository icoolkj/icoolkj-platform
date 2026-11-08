package com.icoolkj.platform.workflow.config;

import com.icoolkj.platform.workflow.support.WorkflowProcessCompletedListener;
import com.icoolkj.platform.workflow.support.WorkflowSubprocessStartedListener;
import java.util.ArrayList;
import java.util.List;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Phase 1 仅使用 Process 引擎；禁用 Event Registry / IDM。
 * <p>
 * MySQL 同实例多库时，Flowable 未指定 catalog 会在 JDBC metadata 中误匹配其它库的 ACT_* 表，
 * 导致空库误判为“已存在”并跳过建表。
 */
@Configuration
public class FlowableEngineConfiguration {

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> processEngineConfigurer(
            Environment environment,
            WorkflowProcessCompletedListener processCompletedListener,
            WorkflowSubprocessStartedListener subprocessStartedListener) {
        return configuration -> {
            configuration.setDisableEventRegistry(true);
            configuration.setDisableIdmEngine(true);
            resolveDatabaseCatalog(environment).ifPresent(configuration::setDatabaseCatalog);
            List<FlowableEventListener> listeners = configuration.getEventListeners();
            if (listeners == null) {
                listeners = new ArrayList<>();
            } else {
                listeners = new ArrayList<>(listeners);
            }
            listeners.add(processCompletedListener);
            listeners.add(subprocessStartedListener);
            configuration.setEventListeners(listeners);
        };
    }

    private static java.util.Optional<String> resolveDatabaseCatalog(Environment environment) {
        String url = environment.getProperty("spring.datasource.url");
        if (!StringUtils.hasText(url)) {
            return java.util.Optional.empty();
        }
        int schemeEnd = url.indexOf("//");
        if (schemeEnd < 0) {
            return java.util.Optional.empty();
        }
        int slash = url.indexOf('/', schemeEnd + 2);
        if (slash < 0 || slash >= url.length() - 1) {
            return java.util.Optional.empty();
        }
        String database = url.substring(slash + 1);
        int query = database.indexOf('?');
        if (query >= 0) {
            database = database.substring(0, query);
        }
        return StringUtils.hasText(database) ? java.util.Optional.of(database) : java.util.Optional.empty();
    }
}

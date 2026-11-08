package com.icoolkj.platform.workflow.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 工作流启动配置。
 * <p>主路径：DB 种子 {@code design_json}/{@code bpmn_xml} → 部署；classpath BPMN 仅为应急。
 */
@Data
@ConfigurationProperties(prefix = "icoolkj.workflow.bootstrap")
public class WorkflowBootstrapProperties {

    /**
     * @deprecated 使用 {@link #classpathDeployEnabled}；若显式配置则覆盖 classpath 部署开关。
     */
    @Deprecated
    private Boolean enabled;

    /** 是否在启动时从 classpath 部署 BPMN（默认关闭，应急开关） */
    private boolean classpathDeployEnabled = false;

    /** 是否将种子/已有模型的 DB BPMN 同步部署到 Flowable（主路径，默认开启） */
    private boolean modelSyncEnabled = true;

    /** 模型无 DB BPMN 时是否回退读取 classpath（默认关闭） */
    private boolean classpathFallbackEnabled = false;

    /** classpath 下的 BPMN 资源路径（仅 classpath 部署 / fallback 使用） */
    private List<String> classpathResources = new ArrayList<>();

    public boolean isClasspathDeployActive() {
        if (enabled != null) {
            return enabled;
        }
        return classpathDeployEnabled;
    }
}

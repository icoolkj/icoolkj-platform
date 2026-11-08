package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.workflow.constant.DesignNodeTypes;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * 抄送任务识别：优先 BPMN {@code category={@link DesignNodeTypes#CC}}；
 * key 前缀 {@code cc} 仅为历史兼容（设计器默认 key）。
 */
public final class WorkflowCcTaskSupport {

    public static final String CATEGORY_CC = DesignNodeTypes.CC;

    private WorkflowCcTaskSupport() {
    }

    public static boolean isCcCategory(String category) {
        return StringUtils.hasText(category)
                && CATEGORY_CC.equalsIgnoreCase(category.trim());
    }

    public static boolean isCcTaskKey(String taskDefinitionKey) {
        if (!StringUtils.hasText(taskDefinitionKey)) {
            return false;
        }
        String key = taskDefinitionKey.trim().toLowerCase(Locale.ROOT);
        return key.equals(DesignNodeTypes.CC) || key.startsWith(DesignNodeTypes.CC);
    }

    public static boolean isCcTask(Task task) {
        if (task == null) {
            return false;
        }
        return isCcCategory(task.getCategory()) || isCcTaskKey(task.getTaskDefinitionKey());
    }

    public static boolean isCcTask(DelegateTask task) {
        if (task == null) {
            return false;
        }
        return isCcCategory(task.getCategory()) || isCcTaskKey(task.getTaskDefinitionKey());
    }

    public static boolean isCcTask(HistoricTaskInstance task) {
        if (task == null) {
            return false;
        }
        return isCcCategory(task.getCategory()) || isCcTaskKey(task.getTaskDefinitionKey());
    }
}

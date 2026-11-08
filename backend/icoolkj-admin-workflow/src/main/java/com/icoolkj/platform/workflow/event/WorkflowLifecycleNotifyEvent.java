package com.icoolkj.platform.workflow.event;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 流程生命周期站内通知事件 */
@Getter
@Setter
public class WorkflowLifecycleNotifyEvent {

    private String modelId;
    private String processInstanceId;
    private String bizId;
    private String bizType;
    private String tenantId;
    /** REJECT / COMPLETE / RETURN / TODO / CC */
    private String eventType;
    private List<String> recipientUserIds;
    private String title;
    private String content;

    public WorkflowLifecycleNotifyEvent() {
    }

    public WorkflowLifecycleNotifyEvent(String modelId, String processInstanceId, String bizId,
                                        String bizType, String tenantId, String eventType,
                                        List<String> recipientUserIds, String title, String content) {
        this.modelId = modelId;
        this.processInstanceId = processInstanceId;
        this.bizId = bizId;
        this.bizType = bizType;
        this.tenantId = tenantId;
        this.eventType = eventType;
        this.recipientUserIds = recipientUserIds;
        this.title = title;
        this.content = content;
    }
}

package com.icoolkj.platform.infra.async;

/** Envelope for Redis Stream transport (local provider only needs {@code taskId}). */
public record AsyncTaskMessage(String taskId, String tenantId, String userId, String locale) {

    public static final String FIELD_TASK_ID = "taskId";
    public static final String FIELD_TENANT_ID = "tenantId";
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_LOCALE = "locale";

    public AsyncTaskMessage {
        taskId = taskId == null ? "" : taskId.trim();
        tenantId = tenantId == null ? "" : tenantId.trim();
        userId = userId == null ? "" : userId.trim();
        locale = locale == null ? "" : locale.trim();
    }

    public static AsyncTaskMessage of(String taskId, String tenantId, String userId) {
        return of(taskId, tenantId, userId, null);
    }

    public static AsyncTaskMessage of(String taskId, String tenantId, String userId, String locale) {
        return new AsyncTaskMessage(taskId, tenantId, userId, locale);
    }
}

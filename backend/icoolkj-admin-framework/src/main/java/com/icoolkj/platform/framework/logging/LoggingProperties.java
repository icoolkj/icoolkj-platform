package com.icoolkj.platform.framework.logging;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "icoolkj.logging")
public class LoggingProperties {

    private AccessLog accessLog = new AccessLog();
    private OperationLog operationLog = new OperationLog();
    private Retention retention = new Retention();
    private AsyncExecutor asyncExecutor = new AsyncExecutor();
    private List<String> sensitiveFields = new ArrayList<>();

    @Data
    public static class AccessLog {

        private boolean enabled = true;
        private boolean includeHeaders = false;
        private boolean includeQueryString = true;
        private boolean asyncSave = true;
        private int maxBodyLength = 4096;
        private List<String> excludeUrls = new ArrayList<>();
    }

    @Data
    public static class OperationLog {

        private boolean enabled = true;
        private boolean asyncSave = true;
    }

    /** Scheduled purge of expired audit logs (append-only in UI; no manual full clear). */
    @Data
    public static class Retention {

        /** Login / operation logs retention in days. */
        private int loginLogDays = 180;
        private int operationLogDays = 180;
        /** API access logs are high-volume; shorter default retention. */
        private int apiAccessLogDays = 90;
        /** Archive table retention after leaving live tables. */
        private int archiveLoginLogDays = 365;
        private int archiveOperationLogDays = 365;
        private int archiveApiAccessLogDays = 180;
    }

    @Data
    public static class AsyncExecutor {

        private int corePoolSize = 2;
        private int maxPoolSize = 8;
        private int queueCapacity = 1000;
        private int awaitTerminationSeconds = 10;
        private String threadNamePrefix = "audit-log-";
    }
}

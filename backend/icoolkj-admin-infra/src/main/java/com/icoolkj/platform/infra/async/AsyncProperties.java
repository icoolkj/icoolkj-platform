package com.icoolkj.platform.infra.async;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

/**
 * Async task runtime. YAML should only set decisions ({@code provider}, {@code consume});
 * queue / retry / DLQ / pool defaults live here — do not copy them into every application.yml.
 */
@Data
@ConfigurationProperties(prefix = "icoolkj.async")
public class AsyncProperties {

    private boolean enabled = true;
    /** {@code local} (in-process) or {@code redis-stream} (API publish / worker consume). */
    private String provider = "local";
    /**
     * When true, this process consumes the Stream (worker only).
     * YAML key: {@code consume}. API omits (default false).
     */
    private boolean consume = false;
    private String queueName = "icoolkj:async:task:stream";
    private String consumerGroup = "icoolkj-worker-group";
    private String consumerName;
    private int localPoolSize = 4;
    private int localQueueCapacity = 200;
    private int pollBatchSize = 5;
    private long pollMs = 2000L;
    private Retry retry = new Retry();
    private DeadLetter deadLetter = new DeadLetter();
    private Reconciliation reconciliation = new Reconciliation();

    /** Legacy YAML {@code consumer-enabled} → {@link #consume}. */
    @DeprecatedConfigurationProperty(reason = "Use icoolkj.async.consume", replacement = "icoolkj.async.consume")
    public void setConsumerEnabled(boolean consumerEnabled) {
        this.consume = consumerEnabled;
    }

    @Deprecated
    public boolean isConsumerEnabled() {
        return consume;
    }

    @Data
    public static class Retry {

        private int maxTimes = 5;
        private long backoffSeconds = 30L;
    }

    @Data
    public static class DeadLetter {

        private boolean enabled = true;
        private String queueName = "icoolkj:async:task:dead-letter";
    }

    @Data
    public static class Reconciliation {

        private boolean enabled = true;
        private long pendingTimeoutSeconds = 120L;
        private long runningTimeoutSeconds = 600L;
        private int maxRequeueAttempts = 3;
        private int batchSize = 50;
        /**
         * After terminal excel-import / excel-export tasks finish, release staging file refs older than this
         * (task rows kept for audit). 0 disables.
         */
        private long excelImportStagingRetentionDays = 7L;
        /**
         * Release {@code excel-import}/{@code excel-export} files whose {@code bizId} has no matching async task
         * (upload-before-insert crash). Age threshold in hours; 0 disables.
         */
        private long excelImportOrphanStagingHours = 6L;
    }
}

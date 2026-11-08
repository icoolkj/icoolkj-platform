package com.icoolkj.platform.infra.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "icoolkj.storage")
public class StorageProperties {

    /** Fallback default engine when sys_config is absent. */
    private String provider = "local";
    private String bucket = "icoolkj";
    private String domain;
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String region = "cn-east-1";
    private boolean pathStyleAccess = true;
    private long signedUrlExpireSeconds = 3600L;

    /** Local disk root directory. */
    private String localRoot = "./data/upload";

    /** Soft-deleted file physical GC (worker scheduled job). */
    private Gc gc = new Gc();

    /** YAML fallbacks for policy (overridden by sys_config when system module is present). */
    private String enabledProviders = "local,minio";
    private long maxFileSizeMb = 100L;
    private String allowedExtensions = "";
    private String blockedExtensions = "exe,bat,sh,cmd,com,msi";

    /** Upload / download security controls. */
    private Security security = new Security();

    @Data
    public static class Gc {

        /** Days to keep soft-deleted metadata + storage object before physical purge. */
        private int retentionDays = 30;
        private int batchSize = 100;
        /** Max batches per scheduled tick (batch-size * max-batches-per-run cap). */
        private int maxBatchesPerRun = 50;
    }

    @Data
    public static class Security {
        private boolean requireBizType = true;
        private boolean contentSniffEnabled = true;
        private boolean verifyMd5OnDownload = true;
        private MalwareScan malwareScan = new MalwareScan();
        private Encryption encryption = new Encryption();
        private RateLimit rateLimit = new RateLimit();
        private Watermark watermark = new Watermark();
        private MinioAcl minioAcl = new MinioAcl();
        private DownloadGuard downloadGuard = new DownloadGuard();
    }

    @Data
    public static class MalwareScan {
        /** Off until ClamAV available; when enabled use clamav. */
        private boolean enabled = false;
        /** clamav | noop */
        private String engine = "clamav";
        private String clamavHost = "127.0.0.1";
        private int clamavPort = 3310;
        private int connectTimeoutMs = 3000;
        private int readTimeoutMs = 60000;
        /** Reject if scanner errors when enabled. */
        private boolean failClosed = true;
    }

    @Data
    public static class Encryption {
        /** App-layer AES-GCM at rest. */
        private boolean enabled = true;
        /** MinIO server-side encryption. */
        private boolean minioSseS3 = false;
    }

    @Data
    public static class RateLimit {
        private boolean enabled = true;
        private int maxUploadsPerMinute = 30;
    }

    @Data
    public static class Watermark {
        private boolean enabled = false;
        private String text = "iCoolKJ";
        /** Apply when sensitivity >= this (default SECRET=2). */
        private String minSensitivity = "2";
    }

    @Data
    public static class MinioAcl {
        private boolean inspectOnStartup = true;
        /** Warn by default. */
        private boolean failOnPublicRead = false;
    }

    /**
     * Elevated download gate for high sensitivity files (lightweight approval substitute).
     */
    @Data
    public static class DownloadGuard {
        private boolean enabled = true;
        /** Require {@link #sensitivePermission} when sensitivity &gt;= this (default CONFIDENTIAL=3). */
        private String sensitiveMinLevel = "3";
        private String sensitivePermission = "infra:file:download:sensitive";
    }
}

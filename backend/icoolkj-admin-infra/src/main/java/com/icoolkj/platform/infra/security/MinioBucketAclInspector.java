package com.icoolkj.platform.infra.security;

import com.icoolkj.platform.common.storage.StorageTypes;
import com.icoolkj.platform.infra.storage.StorageProperties;
import com.icoolkj.platform.infra.storage.minio.MinioClientFactory;
import io.minio.GetBucketPolicyArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Startup check: warn/fail when MinIO bucket policy allows anonymous GetObject.
 */
@Component
public class MinioBucketAclInspector implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MinioBucketAclInspector.class);

    private final StorageProperties storageProperties;
    private final MinioClientFactory minioClientFactory;

    public MinioBucketAclInspector(StorageProperties storageProperties,
                                   MinioClientFactory minioClientFactory) {
        this.storageProperties = storageProperties;
        this.minioClientFactory = minioClientFactory;
    }

    @Override
    public void run(ApplicationArguments args) {
        StorageProperties.MinioAcl acl = storageProperties.getSecurity().getMinioAcl();
        if (!acl.isInspectOnStartup()) {
            return;
        }
        if (!isMinioEnabled()) {
            return;
        }
        if (!StringUtils.hasText(storageProperties.getEndpoint())) {
            return;
        }
        try {
            MinioClient client = minioClientFactory.getClient();
            String bucket = minioClientFactory.bucket();
            String policy = client.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucket).build());
            if (looksPublicReadable(policy)) {
                String msg = "MinIO bucket '" + bucket
                        + "' policy appears to allow public s3:GetObject (Principal *)";
                log.error(msg);
                if (acl.isFailOnPublicRead()) {
                    throw new IllegalStateException(msg);
                }
            }
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("MinIO bucket ACL inspect skipped (MinIO unavailable or no policy): {}",
                    ex.getMessage());
        }
    }

    private boolean isMinioEnabled() {
        String enabled = storageProperties.getEnabledProviders();
        if (StringUtils.hasText(enabled)) {
            for (String part : enabled.split(",")) {
                if (StorageTypes.MINIO.equalsIgnoreCase(part.trim())) {
                    return true;
                }
            }
        }
        return StorageTypes.MINIO.equalsIgnoreCase(storageProperties.getProvider());
    }

    static boolean looksPublicReadable(String policyJson) {
        if (!StringUtils.hasText(policyJson)) {
            return false;
        }
        String normalized = policyJson.replaceAll("\\s+", "");
        boolean principalStar = normalized.contains("\"Principal\":\"*\"")
                || normalized.contains("\"AWS\":\"*\"");
        boolean getObject = normalized.contains("s3:GetObject");
        return principalStar && getObject;
    }
}

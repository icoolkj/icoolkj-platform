package com.icoolkj.platform.infra.storage.minio;

import io.minio.MinioClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.icoolkj.platform.infra.storage.StorageProperties;

@Component
public class MinioClientFactory {

    private final StorageProperties properties;
    private volatile MinioClient client;

    public MinioClientFactory(StorageProperties properties) {
        this.properties = properties;
    }

    public MinioClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = MinioClient.builder()
                            .endpoint(properties.getEndpoint())
                            .credentials(properties.getAccessKey(), properties.getSecretKey())
                            .build();
                }
            }
        }
        return client;
    }

    public String bucket() {
        return properties.getBucket();
    }

    public long signedUrlExpireSeconds() {
        return properties.getSignedUrlExpireSeconds();
    }

    public String publicDomain() {
        if (StringUtils.hasText(properties.getDomain())) {
            return properties.getDomain().replaceAll("/+$", "");
        }
        return properties.getEndpoint().replaceAll("/+$", "");
    }
}

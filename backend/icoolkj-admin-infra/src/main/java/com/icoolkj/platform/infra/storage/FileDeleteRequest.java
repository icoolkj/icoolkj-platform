package com.icoolkj.platform.infra.storage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDeleteRequest {

    private final String storageType;
    private final String bucketName;
    private final String objectKey;
}

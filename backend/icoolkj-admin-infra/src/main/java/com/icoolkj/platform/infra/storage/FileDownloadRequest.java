package com.icoolkj.platform.infra.storage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDownloadRequest {

    private final String storageType;
    private final String bucketName;
    private final String objectKey;
    private final String originalFilename;
    private final String contentType;
}

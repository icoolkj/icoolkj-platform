package com.icoolkj.platform.infra.storage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadRequest {

    private final String tenantId;
    private final String storageType;
    private final String originalFilename;
    private final String contentType;
    private final long size;
    private final java.io.InputStream inputStream;
}

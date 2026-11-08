package com.icoolkj.platform.infra.storage;

import org.springframework.core.io.Resource;

public interface FileStorageService {

    String storageType();

    FileUploadResult upload(FileUploadRequest request);

    void delete(FileDeleteRequest request);

    Resource loadAsResource(FileDownloadRequest request);

    FileDownloadUrlResult previewUrl(FileDownloadUrlRequest request);
}

package com.icoolkj.platform.infra.storage.minio;

import com.icoolkj.platform.common.exception.FileStorageException;
import com.icoolkj.platform.common.storage.StorageTypes;
import com.icoolkj.platform.infra.storage.FileDeleteRequest;
import com.icoolkj.platform.infra.storage.FileDownloadRequest;
import com.icoolkj.platform.infra.storage.FileDownloadUrlRequest;
import com.icoolkj.platform.infra.storage.FileDownloadUrlResult;
import com.icoolkj.platform.infra.storage.FileStorageService;
import com.icoolkj.platform.infra.storage.FileUploadRequest;
import com.icoolkj.platform.infra.storage.FileUploadResult;
import com.icoolkj.platform.infra.storage.StorageProperties;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import io.minio.ServerSideEncryptionS3;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class MinioFileStorageServiceImpl implements FileStorageService {

    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MinioClientFactory clientFactory;
    private final StorageProperties storageProperties;

    public MinioFileStorageServiceImpl(MinioClientFactory clientFactory,
                                       StorageProperties storageProperties) {
        this.clientFactory = clientFactory;
        this.storageProperties = storageProperties;
    }

    @Override
    public String storageType() {
        return StorageTypes.MINIO;
    }

    @Override
    public FileUploadResult upload(FileUploadRequest request) {
        String ext = extractExt(request.getOriginalFilename());
        String storedName = UUID.randomUUID().toString().replace("-", "") + ext;
        String objectKey = request.getTenantId() + "/"
                + LocalDate.now().format(DATE_DIR) + "/"
                + storedName;
        String bucket = clientFactory.bucket();
        MinioClient client = clientFactory.getClient();
        try (InputStream in = request.getInputStream()) {
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(in, request.getSize(), -1)
                    .contentType(StringUtils.hasText(request.getContentType())
                            ? request.getContentType()
                            : "application/octet-stream");
            if (storageProperties.getSecurity().getEncryption().isMinioSseS3()) {
                builder.sse(new ServerSideEncryptionS3());
            }
            client.putObject(builder.build());
        } catch (Exception ex) {
            throw new FileStorageException("MinIO upload failed", ex);
        }
        String fileUrl = clientFactory.publicDomain() + "/" + bucket + "/" + objectKey;
        return FileUploadResult.builder()
                .storageType(StorageTypes.MINIO)
                .bucketName(bucket)
                .objectKey(objectKey)
                .fileUrl(fileUrl)
                .storedFileName(storedName)
                .build();
    }

    @Override
    public void delete(FileDeleteRequest request) {
        try {
            clientFactory.getClient().removeObject(RemoveObjectArgs.builder()
                    .bucket(request.getBucketName())
                    .object(request.getObjectKey())
                    .build());
        } catch (Exception ex) {
            throw new FileStorageException("MinIO delete failed", ex);
        }
    }

    @Override
    public Resource loadAsResource(FileDownloadRequest request) {
        try {
            InputStream stream = clientFactory.getClient().getObject(GetObjectArgs.builder()
                    .bucket(request.getBucketName())
                    .object(request.getObjectKey())
                    .build());
            return new InputStreamResource(stream);
        } catch (Exception ex) {
            throw new FileStorageException("MinIO download failed", ex);
        }
    }

    @Override
    public FileDownloadUrlResult previewUrl(FileDownloadUrlRequest request) {
        try {
            String url = clientFactory.getClient().getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(request.getBucketName())
                    .object(request.getObjectKey())
                    .expiry((int) clientFactory.signedUrlExpireSeconds())
                    .build());
            return FileDownloadUrlResult.builder()
                    .url(url)
                    .external(true)
                    .build();
        } catch (Exception ex) {
            throw new FileStorageException("MinIO preview URL failed", ex);
        }
    }

    private String extractExt(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }
}

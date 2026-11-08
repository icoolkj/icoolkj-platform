package com.icoolkj.platform.infra.storage.local;

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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final String BUCKET = "local";
    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StorageProperties properties;

    public LocalFileStorageServiceImpl(StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String storageType() {
        return StorageTypes.LOCAL;
    }

    @Override
    public FileUploadResult upload(FileUploadRequest request) {
        String ext = extractExt(request.getOriginalFilename());
        String storedName = UUID.randomUUID().toString().replace("-", "") + ext;
        String objectKey = request.getTenantId() + "/"
                + LocalDate.now().format(DATE_DIR) + "/"
                + storedName;
        Path target = resolvePath(objectKey);
        try {
            Files.createDirectories(target.getParent());
            try (InputStream in = request.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new FileStorageException("Local upload failed", ex);
        }
        return FileUploadResult.builder()
                .storageType(StorageTypes.LOCAL)
                .bucketName(BUCKET)
                .objectKey(objectKey)
                .fileUrl(null)
                .storedFileName(storedName)
                .build();
    }

    @Override
    public void delete(FileDeleteRequest request) {
        Path path = resolvePath(request.getObjectKey());
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new FileStorageException("Local delete failed", ex);
        }
    }

    @Override
    public Resource loadAsResource(FileDownloadRequest request) {
        Path path = resolvePath(request.getObjectKey());
        if (!Files.exists(path)) {
            throw new FileStorageException("Local file not found: " + request.getObjectKey());
        }
        return new FileSystemResource(path);
    }

    @Override
    public FileDownloadUrlResult previewUrl(FileDownloadUrlRequest request) {
        return FileDownloadUrlResult.builder()
                .url(null)
                .external(false)
                .build();
    }

    private Path resolvePath(String objectKey) {
        Path root = Paths.get(properties.getLocalRoot()).toAbsolutePath().normalize();
        Path resolved = root.resolve(objectKey).normalize();
        if (!resolved.startsWith(root)) {
            throw new FileStorageException("Invalid object key");
        }
        return resolved;
    }

    private String extractExt(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }
}

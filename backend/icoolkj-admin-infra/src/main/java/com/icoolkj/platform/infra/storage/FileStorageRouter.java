package com.icoolkj.platform.infra.storage;

import com.icoolkj.platform.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FileStorageRouter {

    private final Map<String, FileStorageService> servicesByType;

    public FileStorageRouter(List<FileStorageService> services) {
        this.servicesByType = services.stream()
                .collect(Collectors.toMap(FileStorageService::storageType, Function.identity(), (a, b) -> a));
    }

    public FileStorageService require(String storageType) {
        FileStorageService service = servicesByType.get(storageType);
        if (service == null) {
            throw new BusinessException("error.file.invalid_storage_type");
        }
        return service;
    }
}

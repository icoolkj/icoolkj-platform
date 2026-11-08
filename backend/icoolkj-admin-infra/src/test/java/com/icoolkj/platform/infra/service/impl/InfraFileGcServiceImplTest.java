package com.icoolkj.platform.infra.service.impl;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.infra.domain.InfraFileEntity;
import com.icoolkj.platform.infra.mapper.InfraFileMapper;
import com.icoolkj.platform.infra.mapper.InfraFileRefMapper;
import com.icoolkj.platform.infra.storage.FileStorageRouter;
import com.icoolkj.platform.infra.storage.FileStorageService;
import com.icoolkj.platform.infra.storage.StorageProperties;
import com.icoolkj.platform.infra.vo.InfraFileGcResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InfraFileGcServiceImplTest {

    @Mock
    private InfraFileMapper infraFileMapper;
    @Mock
    private InfraFileRefMapper infraFileRefMapper;
    @Mock
    private FileStorageRouter fileStorageRouter;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private StorageProperties storageProperties;

    @InjectMocks
    private InfraFileGcServiceImpl infraFileGcService;

    @Test
    void purgeExpiredBatch_skipsWhenActiveRefsRemain() {
        StorageProperties.Gc gc = new StorageProperties.Gc();
        gc.setRetentionDays(30);
        gc.setBatchSize(10);
        when(storageProperties.getGc()).thenReturn(gc);

        InfraFileEntity entity = new InfraFileEntity();
        entity.setId("1001");
        entity.setTenantId(TenantConstants.PLATFORM_TENANT_ID);
        entity.setStorageType("local");
        entity.setBucketName("local");
        entity.setObjectKey("/tmp/a.jpg");
        when(infraFileMapper.selectSoftDeletedBefore(any(LocalDateTime.class), eq(10)))
                .thenReturn(List.of(entity));
        when(infraFileRefMapper.countActiveByFileId(TenantConstants.PLATFORM_TENANT_ID, "1001")).thenReturn(1L);

        InfraFileGcResult result = infraFileGcService.purgeExpiredBatch();

        assertEquals(0, result.getPurged());
        assertEquals(1, result.getSkipped());
        verify(fileStorageRouter, never()).require(any());
        verify(infraFileMapper, never()).deleteById(any());
    }

    @Test
    void purgeExpiredBatch_purgesWhenUnreferenced() {
        StorageProperties.Gc gc = new StorageProperties.Gc();
        gc.setRetentionDays(30);
        gc.setBatchSize(10);
        when(storageProperties.getGc()).thenReturn(gc);

        InfraFileEntity entity = new InfraFileEntity();
        entity.setId("1002");
        entity.setTenantId(TenantConstants.PLATFORM_TENANT_ID);
        entity.setStorageType("local");
        entity.setBucketName("local");
        entity.setObjectKey("/tmp/b.jpg");
        when(infraFileMapper.selectSoftDeletedBefore(any(LocalDateTime.class), eq(10)))
                .thenReturn(List.of(entity));
        when(infraFileRefMapper.countActiveByFileId(TenantConstants.PLATFORM_TENANT_ID, "1002")).thenReturn(0L);
        when(fileStorageRouter.require("local")).thenReturn(fileStorageService);

        InfraFileGcResult result = infraFileGcService.purgeExpiredBatch();

        assertEquals(1, result.getPurged());
        assertEquals(0, result.getSkipped());
        verify(infraFileMapper).deleteById("1002");
    }
}

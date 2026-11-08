package com.icoolkj.platform.infra.service.impl;

import com.icoolkj.platform.common.exception.FileStorageException;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.infra.domain.InfraFileEntity;
import com.icoolkj.platform.infra.mapper.InfraFileMapper;
import com.icoolkj.platform.infra.mapper.InfraFileRefMapper;
import com.icoolkj.platform.infra.service.InfraFileGcService;
import com.icoolkj.platform.infra.storage.FileDeleteRequest;
import com.icoolkj.platform.infra.storage.FileStorageRouter;
import com.icoolkj.platform.infra.storage.FileStorageService;
import com.icoolkj.platform.infra.storage.StorageProperties;
import com.icoolkj.platform.infra.vo.InfraFileGcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/** 软删文件到期物理回收 */
@Service
public class InfraFileGcServiceImpl implements InfraFileGcService {

    private static final Logger log = LoggerFactory.getLogger(InfraFileGcServiceImpl.class);

    private final InfraFileMapper infraFileMapper;
    private final InfraFileRefMapper infraFileRefMapper;
    private final FileStorageRouter fileStorageRouter;
    private final StorageProperties storageProperties;

    public InfraFileGcServiceImpl(InfraFileMapper infraFileMapper,
                                  InfraFileRefMapper infraFileRefMapper,
                                  FileStorageRouter fileStorageRouter,
                                  StorageProperties storageProperties) {
        this.infraFileMapper = infraFileMapper;
        this.infraFileRefMapper = infraFileRefMapper;
        this.fileStorageRouter = fileStorageRouter;
        this.storageProperties = storageProperties;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InfraFileGcResult purgeExpiredBatch() {
        StorageProperties.Gc gc = storageProperties.getGc();
        LocalDateTime before = LocalDateTime.now().minusDays(Math.max(gc.getRetentionDays(), 1));
        int batchSize = Math.max(gc.getBatchSize(), 1);

        return TenantScopeRunner.runIgnoringTenant(() -> {
            List<InfraFileEntity> candidates = infraFileMapper.selectSoftDeletedBefore(before, batchSize);
            if (CollectionUtils.isEmpty(candidates)) {
                return new InfraFileGcResult(0, 0, 0);
            }
            int purged = 0;
            int skipped = 0;
            int failed = 0;
            for (InfraFileEntity entity : candidates) {
                try {
                    if (infraFileRefMapper.countActiveByFileId(entity.getTenantId(), entity.getId()) > 0) {
                        skipped++;
                        log.warn("Skip file GC, active refs remain: fileId={}, tenantId={}",
                                entity.getId(), entity.getTenantId());
                        continue;
                    }
                    deletePhysical(entity);
                    infraFileRefMapper.deleteByFileId(entity.getTenantId(), entity.getId());
                    infraFileMapper.deleteById(entity.getId());
                    purged++;
                } catch (RuntimeException ex) {
                    failed++;
                    log.warn("File GC failed for fileId={}: {}", entity.getId(), ex.getMessage(), ex);
                }
            }
            return new InfraFileGcResult(purged, skipped, failed);
        });
    }

    private void deletePhysical(InfraFileEntity entity) {
        try {
            FileStorageService storage = fileStorageRouter.require(entity.getStorageType());
            storage.delete(FileDeleteRequest.builder()
                    .storageType(entity.getStorageType())
                    .bucketName(entity.getBucketName())
                    .objectKey(entity.getObjectKey())
                    .build());
        } catch (FileStorageException ex) {
            throw new RuntimeException("Storage delete failed: " + ex.getMessage(), ex);
        }
    }
}

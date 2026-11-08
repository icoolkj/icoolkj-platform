package com.icoolkj.platform.system.backup.service.impl;

import com.icoolkj.platform.common.backup.DatabaseBackupSettingsProvider;
import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.ConfigKeys;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.job.ManagedJobCatalog;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.system.config.domain.ConfigEntity;
import com.icoolkj.platform.system.job.domain.SysJobEntity;
import com.icoolkj.platform.system.backup.dto.DatabaseBackupConfigUpdateRequest;
import com.icoolkj.platform.system.config.mapper.ConfigMapper;
import com.icoolkj.platform.system.job.mapper.SysJobMapper;
import com.icoolkj.platform.system.config.service.ConfigService;
import com.icoolkj.platform.system.backup.service.DatabaseBackupConfigAdminService;
import com.icoolkj.platform.system.backup.vo.DatabaseBackupConfigAdminVO;
import java.nio.file.Paths;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Primary
public class DatabaseBackupConfigAdminServiceImpl implements DatabaseBackupConfigAdminService, DatabaseBackupSettingsProvider {

    private final ConfigService configService;
    private final ConfigMapper configMapper;
    private final SysJobMapper sysJobMapper;
    private final YamlDatabaseBackupSettingsFallback yamlFallback;

    public DatabaseBackupConfigAdminServiceImpl(ConfigService configService,
                                                  ConfigMapper configMapper,
                                                  SysJobMapper sysJobMapper,
                                                  YamlDatabaseBackupSettingsFallback yamlFallback) {
        this.configService = configService;
        this.configMapper = configMapper;
        this.sysJobMapper = sysJobMapper;
        this.yamlFallback = yamlFallback;
    }

    @Override
    public DatabaseBackupConfigAdminVO getForAdmin() {
        DatabaseBackupConfigAdminVO vo = new DatabaseBackupConfigAdminVO();
        vo.setEnabled(isEnabled());
        vo.setCron(getCron());
        vo.setOutputDir(Paths.get(getOutputDir()).toAbsolutePath().normalize().toString());
        vo.setRetentionDays(getRetentionDays());
        vo.setCompress(isCompress());
        vo.setMysqldumpCommand(yamlFallback.getMysqldumpCommand());
        vo.setMysqldumpFromYaml(true);
        return vo;
    }

    @Override
    public void update(DatabaseBackupConfigUpdateRequest request) {
        validateCron(request.getCron());
        String outputDir = request.getOutputDir().trim();
        if (!StringUtils.hasText(outputDir)) {
            throw new BusinessException("error.validation.required", "fields.outputDir");
        }
        String tenantId = TenantConstants.PLATFORM_TENANT_ID;
        upsertJobSchedule(request.isEnabled(), request.getCron().trim());
        upsert(tenantId, ConfigKeys.BACKUP_OUTPUT_DIR, outputDir);
        upsert(tenantId, ConfigKeys.BACKUP_RETENTION_DAYS, String.valueOf(request.getRetentionDays()));
        upsert(tenantId, ConfigKeys.BACKUP_COMPRESS, String.valueOf(request.isCompress()));
    }

    @Override
    public boolean isEnabled() {
        SysJobEntity job = sysJobMapper.selectByJobCode(ManagedJobCatalog.DATABASE_BACKUP);
        return job != null && CommonConstants.STATUS_NORMAL.equals(job.getStatus());
    }

    @Override
    public String getCron() {
        SysJobEntity job = sysJobMapper.selectByJobCode(ManagedJobCatalog.DATABASE_BACKUP);
        if (job != null && StringUtils.hasText(job.getCronExpression())) {
            return job.getCronExpression().trim();
        }
        return "";
    }

    @Override
    public String getOutputDir() {
        return configService.getString(
                TenantConstants.PLATFORM_TENANT_ID,
                ConfigKeys.BACKUP_OUTPUT_DIR,
                yamlFallback.getOutputDir()
        );
    }

    @Override
    public int getRetentionDays() {
        return configService.getInt(
                TenantConstants.PLATFORM_TENANT_ID,
                ConfigKeys.BACKUP_RETENTION_DAYS,
                yamlFallback.getRetentionDays()
        );
    }

    @Override
    public boolean isCompress() {
        return configService.getBoolean(
                TenantConstants.PLATFORM_TENANT_ID,
                ConfigKeys.BACKUP_COMPRESS,
                yamlFallback.isCompress()
        );
    }

    private void upsertJobSchedule(boolean enabled, String cron) {
        SysJobEntity job = sysJobMapper.selectByJobCode(ManagedJobCatalog.DATABASE_BACKUP);
        if (job == null) {
            throw new BusinessException("error.job.unknown");
        }
        job.setCronExpression(cron);
        job.setStatus(enabled ? CommonConstants.STATUS_NORMAL : CommonConstants.STATUS_DISABLE);
        AuditFieldHelper.fillUpdate(job);
        sysJobMapper.updateById(job);
    }

    private void validateCron(String cron) {
        if (!StringUtils.hasText(cron)) {
            throw new BusinessException("error.backup.invalid_cron");
        }
        try {
            CronExpression.parse(cron.trim());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("error.backup.invalid_cron");
        }
    }

    private void upsert(String tenantId, String configKey, String configValue) {
        ConfigEntity entity = configMapper.selectByTenantAndKey(tenantId, configKey);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.config");
        }
        entity.setConfigValue(configValue);
        configMapper.updateById(entity);
        configService.evict(tenantId, configKey);
    }
}

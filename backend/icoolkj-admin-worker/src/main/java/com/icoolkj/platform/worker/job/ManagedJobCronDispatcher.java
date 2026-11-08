package com.icoolkj.platform.worker.job;

import com.icoolkj.platform.framework.job.JobRunner;
import com.icoolkj.platform.framework.job.JobTriggerType;
import com.icoolkj.platform.framework.job.ManagedJobCatalog;
import com.icoolkj.platform.framework.job.ManagedJobCommand;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.system.job.domain.SysJobEntity;
import com.icoolkj.platform.system.job.mapper.SysJobMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Evaluates enabled {@code sys_job} rows each minute and runs due jobs via {@link JobRunner}.
 */
@Component
public class ManagedJobCronDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ManagedJobCronDispatcher.class);
    private static final DateTimeFormatter MINUTE_SLOT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final SysJobMapper sysJobMapper;
    private final JobRunner jobRunner;
    private final ConcurrentMap<String, String> lastFiredMinuteByJob = new ConcurrentHashMap<>();

    public ManagedJobCronDispatcher(SysJobMapper sysJobMapper, JobRunner jobRunner) {
        this.sysJobMapper = sysJobMapper;
        this.jobRunner = jobRunner;
    }

    @Scheduled(cron = "0 * * * * ?")
    public void tick() {
        // Same as JobRunner: no HTTP tenant on the scheduler thread.
        TenantScopeRunner.runIgnoringTenant(this::tickIgnoringTenant);
    }

    private void tickIgnoringTenant() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String slot = now.format(MINUTE_SLOT);
        List<SysJobEntity> jobs = sysJobMapper.selectAllEnabled();
        if (jobs == null || jobs.isEmpty()) {
            return;
        }
        for (SysJobEntity job : jobs) {
            String jobCode = job.getJobCode();
            if (!StringUtils.hasText(jobCode) || !ManagedJobCatalog.isKnown(jobCode)) {
                continue;
            }
            if (!isDue(job.getCronExpression(), now)) {
                continue;
            }
            if (slot.equals(lastFiredMinuteByJob.get(jobCode))) {
                continue;
            }
            lastFiredMinuteByJob.put(jobCode, slot);
            jobRunner.run(new ManagedJobCommand(jobCode.trim(), JobTriggerType.CRON, null));
        }
    }

    private static boolean isDue(String cron, LocalDateTime nowMinute) {
        if (!StringUtils.hasText(cron)) {
            return false;
        }
        CronExpression expression;
        try {
            expression = CronExpression.parse(cron.trim());
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid sys_job.cron_expression: {}", cron);
            return false;
        }
        LocalDateTime next = expression.next(nowMinute.minusMinutes(1));
        if (next == null) {
            return false;
        }
        return next.truncatedTo(ChronoUnit.MINUTES).equals(nowMinute);
    }
}

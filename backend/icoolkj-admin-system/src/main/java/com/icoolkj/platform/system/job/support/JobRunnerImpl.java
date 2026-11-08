package com.icoolkj.platform.system.job.support;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.job.JobContext;
import com.icoolkj.platform.framework.job.JobHandler;
import com.icoolkj.platform.framework.job.JobRunner;
import com.icoolkj.platform.framework.job.JobTriggerType;
import com.icoolkj.platform.framework.job.ManagedJobCommand;
import com.icoolkj.platform.framework.job.ManagedJobTriggerService;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.system.job.domain.SysJobEntity;
import com.icoolkj.platform.system.job.domain.SysJobLogEntity;
import com.icoolkj.platform.system.job.mapper.SysJobLogMapper;
import com.icoolkj.platform.system.job.mapper.SysJobMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Gates by {@code sys_job.status} / concurrency, writes {@code sys_job_log}, invokes {@link JobHandler}.
 */
@Service
public class JobRunnerImpl implements JobRunner {

    private static final Logger log = LoggerFactory.getLogger(JobRunnerImpl.class);
    private static final String ALLOW_CONCURRENT = "1";

    private final SysJobMapper sysJobMapper;
    private final SysJobLogMapper sysJobLogMapper;
    private final ManagedJobTriggerService triggerService;
    private final Map<String, JobHandler> handlersByCode;

    public JobRunnerImpl(SysJobMapper sysJobMapper,
                         SysJobLogMapper sysJobLogMapper,
                         ManagedJobTriggerService triggerService,
                         List<JobHandler> handlers) {
        this.sysJobMapper = sysJobMapper;
        this.sysJobLogMapper = sysJobLogMapper;
        this.triggerService = triggerService;
        this.handlersByCode = handlers.stream()
                .collect(Collectors.toMap(JobHandler::jobCode, Function.identity(), (a, b) -> {
                    throw new IllegalStateException("Duplicate JobHandler for code: " + a.jobCode());
                }));
    }

    @Override
    public boolean run(ManagedJobCommand command) {
        // Worker scheduling threads have no HTTP tenant; without ignore, tenant_id becomes -1
        // and sys_job (platform tenant 0) / cross-tenant cleanup handlers cannot see rows.
        return TenantScopeRunner.runIgnoringTenant(() -> doRun(command));
    }

    private boolean doRun(ManagedJobCommand command) {
        if (command == null || !StringUtils.hasText(command.jobCode())) {
            return false;
        }
        String jobCode = command.jobCode().trim();
        SysJobEntity job = sysJobMapper.selectByJobCode(jobCode);
        if (job == null) {
            log.warn("Skip job run, sys_job row missing: {}", jobCode);
            triggerService.markFailed(jobCode, "job missing");
            return false;
        }
        if (!CommonConstants.STATUS_NORMAL.equals(job.getStatus())) {
            log.debug("Skip paused job: {}", jobCode);
            // Clear Redis QUEUED so the console is not stuck after run-now on a paused job.
            triggerService.markFailed(jobCode, "paused");
            return false;
        }
        JobHandler handler = handlersByCode.get(jobCode);
        if (handler == null) {
            log.error("No JobHandler registered for job_code={}", jobCode);
            triggerService.markFailed(jobCode, "handler missing");
            return false;
        }
        if (!ALLOW_CONCURRENT.equals(job.getAllowConcurrent())
                && sysJobLogMapper.countRunningByJobCode(jobCode) > 0) {
            log.info("Skip concurrent run for job_code={}", jobCode);
            triggerService.markFailed(jobCode, "already running");
            return false;
        }

        LocalDateTime startedAt = LocalDateTime.now();
        SysJobLogEntity jobLog = new SysJobLogEntity();
        jobLog.setId(IdGeneratorUtils.nextIdStr());
        jobLog.setTenantId(TenantConstants.PLATFORM_TENANT_ID);
        jobLog.setJobId(job.getId());
        jobLog.setJobCode(jobCode);
        jobLog.setTriggerType(command.triggerType() == null
                ? JobTriggerType.CRON.name()
                : command.triggerType().name());
        jobLog.setTriggerUserId(command.triggerUserId());
        jobLog.setStatus("RUNNING");
        jobLog.setStartedAt(startedAt);
        jobLog.setCreatedAt(startedAt);
        sysJobLogMapper.insert(jobLog);

        triggerService.markRunning(jobCode);
        JobContext context = new JobContext(jobCode, command.triggerType(), command.triggerUserId());
        try {
            handler.execute(context);
            jobLog.setStatus("SUCCESS");
            jobLog.setJobMessage("ok");
            jobLog.setRequestParam(context.getRequestParam());
            jobLog.setResultSummary(context.getResultSummary());
            jobLog.setFinishedAt(LocalDateTime.now());
            sysJobLogMapper.updateById(jobLog);
            triggerService.markSuccess(jobCode, "ok");
            return true;
        } catch (Exception ex) {
            log.error("Job failed: {}", jobCode, ex);
            jobLog.setStatus("FAILED");
            jobLog.setJobMessage(trimMessage(ex.getMessage()));
            jobLog.setExceptionInfo(trimException(ex));
            jobLog.setRequestParam(context.getRequestParam());
            jobLog.setResultSummary(context.getResultSummary());
            jobLog.setFinishedAt(LocalDateTime.now());
            sysJobLogMapper.updateById(jobLog);
            triggerService.markFailed(jobCode, trimMessage(ex.getMessage()));
            return true;
        }
    }

    private static String trimMessage(String message) {
        if (!StringUtils.hasText(message)) {
            return "failed";
        }
        return message.length() > 480 ? message.substring(0, 480) : message;
    }

    private static String trimException(Exception ex) {
        String text = ex.toString();
        if (ex.getCause() != null) {
            text = text + " | cause=" + ex.getCause();
        }
        return text.length() > 4000 ? text.substring(0, 4000) : text;
    }
}

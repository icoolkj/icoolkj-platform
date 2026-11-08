package com.icoolkj.platform.worker.job;

import com.icoolkj.platform.framework.job.JobRunner;
import com.icoolkj.platform.framework.job.JobTriggerType;
import com.icoolkj.platform.framework.job.ManagedJobCommand;
import com.icoolkj.platform.framework.job.ManagedJobTriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Consumes Redis run-now commands and executes via {@link JobRunner} (writes sys_job_log).
 */
@Component
public class ManagedJobCommandConsumer {

    private static final Logger log = LoggerFactory.getLogger(ManagedJobCommandConsumer.class);

    private final ManagedJobTriggerService triggerService;
    private final JobRunner jobRunner;

    public ManagedJobCommandConsumer(ManagedJobTriggerService triggerService, JobRunner jobRunner) {
        this.triggerService = triggerService;
        this.jobRunner = jobRunner;
    }

    @Scheduled(fixedDelayString = "${icoolkj.job.command-poll-ms:3000}")
    public void poll() {
        for (int i = 0; i < 5; i++) {
            ManagedJobCommand command = triggerService.poll();
            if (command == null) {
                return;
            }
            if (command.triggerType() == null) {
                command = new ManagedJobCommand(command.jobCode(), JobTriggerType.MANUAL, command.triggerUserId());
            }
            boolean started = jobRunner.run(command);
            if (!started) {
                log.debug("Managed job command skipped: {}", command.jobCode());
            }
        }
    }
}

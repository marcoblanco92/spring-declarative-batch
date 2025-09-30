package com.marbl.declarative_batct.spring_declarative_batch.configuration;

import com.marbl.declarative_batct.spring_declarative_batch.factory.job.JobFactory;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.BatchJobConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobExecutor {

    private final BatchJobConfig jobConfig;
    private final JobFactory jobFactory;
    private final JobLauncher jobLauncher;

    /**
     * Create and run a Spring Batch Job from YAML config
     */
    public JobExecution runJob() {
        try {
            // 1️⃣ Crea il job
            Job job = jobFactory.createJob(jobConfig);

            // 2️⃣ Crea parametri di esecuzione
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis()) // param unico per garantire esecuzione nuova
                    .toJobParameters();

            // 3️⃣ Lancia il job
            log.info("Launching job '{}'", job.getName());
            JobExecution execution = jobLauncher.run(job, params);
            log.info("Job '{}' launched successfully with status {}", job.getName(), execution.getStatus());

            return execution;

        } catch (JobExecutionAlreadyRunningException e) {
            log.error("Job '{}' is already running", jobConfig.getName(), e);
        } catch (JobRestartException e) {
            log.error("Job '{}' cannot be restarted", jobConfig.getName(), e);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("Job '{}' has already completed", jobConfig.getName(), e);
        } catch (JobParametersInvalidException e) {
            log.error("Job '{}' parameters are invalid", jobConfig.getName(), e);
        } catch (Exception e) {
            log.error("Unexpected error while running job '{}'", jobConfig.getName(), e);
        }

        return null;
    }
}

package com.marbl.declarative_batct.spring_declarative_batch;

import com.marbl.declarative_batct.spring_declarative_batch.model.support.executor.BatchJobExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
@ConfigurationPropertiesScan
public class SpringDeclarativeBatchApplication implements CommandLineRunner {

    private final BatchJobExecutor jobExecutor;

    public static void main(String[] args) {
        SpringApplication.run(SpringDeclarativeBatchApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("Starting batch job from @SpringBootApplication...");

        JobExecution execution = jobExecutor.runJob();
        log.info("Job execution status: {}", execution.getStatus());
    }
}


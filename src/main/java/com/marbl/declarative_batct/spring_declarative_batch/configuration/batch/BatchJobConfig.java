package com.marbl.declarative_batct.spring_declarative_batch.configuration.batch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
@ConfigurationProperties(prefix = "bulk.batch-job")
public class BatchJobConfig {

    @NotEmpty(message = "BatchJob name is required")
    private String name;

    @Valid
    private ListenerConfig listener;

    @Valid
    @NotEmpty(message = "BatchJob must have at least one step")
    private List<StepsConfig> steps;

}


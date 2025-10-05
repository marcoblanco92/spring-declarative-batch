package com.marbl.declarative_batct.spring_declarative_batch.configuration.datasource;

import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
@ConfigurationProperties(prefix = "bulk")
public class BatchDatabaseConfig {

    @Valid
    List<DatasourceConfig> datasources;
}

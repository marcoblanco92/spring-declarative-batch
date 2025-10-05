package com.marbl.declarative_batct.spring_declarative_batch.configuration.writer;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
public class JdbcBatchWriterConfig {

    @NotBlank(message = "Datasource is required")
    private String datasource;

    @NotBlank(message = "Sql is required")
    private String sql;

    @NotBlank(message = "PreparedStatementClass is required")
    private String preparedStatementClass;
}

package com.marbl.declarative_batct.spring_declarative_batch.model.support.reader;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JdbcReaderConfig implements ReaderConfig {

    @NotBlank(message = "Datasource is required")
    private String datasource;

    @NotBlank(message = "Sql is required")
    private String sql;

    @NotBlank(message = "PreparedStatementClass is required")
    private String preparedStatementClass;

    @NotBlank(message = "MappedClass is require")
    private String mappedClass;
}
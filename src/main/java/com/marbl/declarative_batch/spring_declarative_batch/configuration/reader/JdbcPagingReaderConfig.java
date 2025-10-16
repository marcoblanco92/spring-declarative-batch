package com.marbl.declarative_batch.spring_declarative_batch.configuration.reader;

import com.marbl.declarative_batch.spring_declarative_batch.enums.PagingProviderType;
import com.marbl.declarative_batch.spring_declarative_batch.model.PagingSqlModel;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class JdbcPagingReaderConfig implements ReaderConfig {

    @NotBlank(message = "'datasource' must be provided")
    private String datasource;

    @NotBlank(message = "'preparedStatementClass' must be provided")
    private String preparedStatementClass;

    @NotBlank(message = "'providerType' must be provided")
    private PagingProviderType providerType;

    @NotBlank(message = "'mappedClass' must be provided")
    private String mappedClass;

    private PagingSqlModel clause;

    private Map<String, Object> parameters;
}

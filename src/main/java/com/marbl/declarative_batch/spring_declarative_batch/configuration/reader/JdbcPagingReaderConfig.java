package com.marbl.declarative_batch.spring_declarative_batch.configuration.reader;

import com.marbl.declarative_batch.spring_declarative_batch.enums.PagingProviderType;
import com.marbl.declarative_batch.spring_declarative_batch.model.PagingSqlModel;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class JdbcPagingReaderConfig implements ReaderConfig {

    @NotBlank(message = "Datasource is required")
    private String datasource;

    @NotBlank(message = "PreparedStatementClass is required")
    private String preparedStatementClass;

    @NotBlank(message = "ProviderType is required")
    private PagingProviderType providerType;

    @NotBlank(message = "MappedClass is require")
    private String mappedClass;

    private PagingSqlModel clause;

    private Map<String, Object> parameters;
}
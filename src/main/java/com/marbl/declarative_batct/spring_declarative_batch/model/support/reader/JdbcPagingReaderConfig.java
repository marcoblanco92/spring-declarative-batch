package com.marbl.declarative_batct.spring_declarative_batch.model.support.reader;

import com.marbl.declarative_batct.spring_declarative_batch.enums.PagingProviderType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.batch.item.database.Order;

import java.util.Map;

@Data
public class JdbcPagingReaderConfig implements ReaderConfig {

    @NotBlank(message = "Datasource is required")
    private String datasource;

    @NotBlank(message = "Sql is required")
    private String sql;

    @NotBlank(message = "PreparedStatementClass is required")
    private String preparedStatementClass;

    @NotBlank(message = "ProviderType is required")
    private PagingProviderType providerType;

    @NotBlank(message = "MappedClass is require")
    private String mappedClass;

    private String selectClause;
    private String fromClause;
    private String whereClause;
    private String orderByClause;

    private Map<String, Order> sortKeys;
    private Map<String, Object> parameters;
}
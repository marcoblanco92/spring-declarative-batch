package com.marbl.declarative_batct.spring_declarative_batch.builder.reader;

import com.marbl.declarative_batct.spring_declarative_batch.model.support.ComponentConfig;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.reader.JdbcPagingReaderConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.Map;

import static com.marbl.declarative_batct.spring_declarative_batch.utils.ReflectionUtils.instantiateClass;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcPagingReaderBuilding {

    public static <T> JdbcPagingItemReader<T> build(ComponentConfig config, ApplicationContext context) {
        try {
            JdbcPagingReaderConfig jdbcConfig = (JdbcPagingReaderConfig) config.getConfig();
            DataSource ds = context.getBean(jdbcConfig.getDatasource(), DataSource.class);
            RowMapper<T> rowMapper = instantiateClass(jdbcConfig.getMappedClass(), RowMapper.class);

            JdbcPagingItemReader<T> reader = new JdbcPagingItemReader<>();
            reader.setName(config.getName());
            reader.setDataSource(ds);
            reader.setRowMapper(rowMapper);
            reader.setPageSize(context.getEnvironment().getProperty("batch-job.chunkSize", Integer.class, 10));

            // Build paging query provider (vendor-specific, here MySQL)
            MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
            queryProvider.setSelectClause(jdbcConfig.getSelectClause());
            queryProvider.setFromClause(jdbcConfig.getFromClause());
            queryProvider.setWhereClause(jdbcConfig.getWhereClause());

            // Sorting is mandatory for deterministic paging
            Map<String, Order> sortKeys = jdbcConfig.getSortKeys();
            queryProvider.setSortKeys(sortKeys);

            Map<String,Object> parameters = jdbcConfig.getParameters();
            reader.setParameterValues(parameters);
            reader.afterPropertiesSet();

            return reader;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to create JdbcPagingItemReader for config=" + config.getName(), e
            );
        }
    }
}


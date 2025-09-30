package com.marbl.declarative_batct.spring_declarative_batch.builder.reader;


import com.marbl.declarative_batct.spring_declarative_batch.model.support.ComponentConfig;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.reader.JdbcCursorReaderConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

import static com.marbl.declarative_batct.spring_declarative_batch.utils.ReflectionUtils.instantiateClass;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcCursorReaderBuilder {

    public static <T> JdbcCursorItemReader<T> build(ComponentConfig config, ApplicationContext context) {
        try {
            JdbcCursorReaderConfig jdbcConfig = (JdbcCursorReaderConfig) config.getConfig();
            DataSource ds = context.getBean(jdbcConfig.getDatasource(), DataSource.class);

            // Load RowMapper class via reflection
            RowMapper<T> rowMapper = instantiateClass(jdbcConfig.getMappedClass(), RowMapper.class);

            // Load PreparedStatementSetter class via reflection
            PreparedStatementSetter psSetter = instantiateClass(jdbcConfig.getPreparedStatementClass(), PreparedStatementSetter.class);

            // Use builder instead of direct setters for better readability
            return new JdbcCursorItemReaderBuilder<T>()
                    .name(config.getName())
                    .dataSource(ds)
                    .sql(jdbcConfig.getSql())
                    .rowMapper(rowMapper)
                    .preparedStatementSetter(psSetter)
                    .build();

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to create JdbcCursorItemReader for config=" + config.getName(), e
            );
        }
    }
}



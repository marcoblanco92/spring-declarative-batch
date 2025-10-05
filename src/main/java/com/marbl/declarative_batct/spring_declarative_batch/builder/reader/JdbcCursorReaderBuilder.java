package com.marbl.declarative_batct.spring_declarative_batch.builder.reader;


import com.marbl.declarative_batct.spring_declarative_batch.configuration.batch.ComponentConfig;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.reader.JdbcCursorReaderConfig;
import com.marbl.declarative_batct.spring_declarative_batch.utils.ReflectionUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcCursorReaderBuilder {

    public static <I> JdbcCursorItemReader<I> build(ComponentConfig config, ApplicationContext context) {
        try {
            JdbcCursorReaderConfig jdbcConfig = (JdbcCursorReaderConfig) config.getConfig();

            DataSource ds = context.getBean(jdbcConfig.getDatasource(), DataSource.class);

            // Instantiate RowMapper and PreparedStatementSetter via ReflectionUtils
            RowMapper<I> rowMapper = ReflectionUtils.instantiateClass(jdbcConfig.getMappedClass(), RowMapper.class);
            PreparedStatementSetter psSetter = ReflectionUtils.instantiateClass(jdbcConfig.getPreparedStatementClass(), PreparedStatementSetter.class);

            return new JdbcCursorItemReaderBuilder<I>()
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



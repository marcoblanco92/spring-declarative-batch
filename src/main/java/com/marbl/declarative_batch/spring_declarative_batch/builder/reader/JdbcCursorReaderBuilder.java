package com.marbl.declarative_batch.spring_declarative_batch.builder.reader;


import com.marbl.declarative_batch.spring_declarative_batch.configuration.batch.ComponentConfig;
import com.marbl.declarative_batch.spring_declarative_batch.configuration.reader.JdbcCursorReaderConfig;
import com.marbl.declarative_batch.spring_declarative_batch.utils.DatasourceUtils;
import com.marbl.declarative_batch.spring_declarative_batch.utils.MapUtils;
import com.marbl.declarative_batch.spring_declarative_batch.utils.ReflectionUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcCursorReaderBuilder {

    public static <I> JdbcCursorItemReader<I> build(ComponentConfig config, ApplicationContext context) {
        try {

            // Normalize the map structure (convert numeric-keyed maps to lists)
            Object normalizedMap = MapUtils.normalizeMapStructure(config.getConfig());
            log.debug("Normalized configuration map for JdbcCursorReader: {}", normalizedMap);

            // Convert normalized map to the target DTO
            JdbcCursorReaderConfig jdbcConfig = MapUtils.mapToConfigDto(normalizedMap, JdbcCursorReaderConfig.class);
            log.debug("Converted configuration map to JdbcCursorReaderConfig DTO: {}", jdbcConfig);

            DataSource ds = DatasourceUtils.getDataSource(context, jdbcConfig.getDatasource());

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



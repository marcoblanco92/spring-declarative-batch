package com.marbl.declarative_batct.spring_declarative_batch.builder.reader;

import com.marbl.declarative_batct.spring_declarative_batch.configuration.batch.ComponentConfig;
import com.marbl.declarative_batct.spring_declarative_batch.configuration.reader.JdbcCursorReaderConfig;
import com.marbl.declarative_batct.spring_declarative_batch.configuration.reader.JdbcPagingReaderConfig;
import com.marbl.declarative_batct.spring_declarative_batch.utils.DatasourceUtils;
import com.marbl.declarative_batct.spring_declarative_batch.utils.MapUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.Map;

import static com.marbl.declarative_batct.spring_declarative_batch.utils.ReflectionUtils.instantiateClass;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcPagingReaderBuilder {

    public static <I> JdbcPagingItemReader<I> build(ComponentConfig config, ApplicationContext context, int chunk) {
        try {

            // Normalize the map structure (convert numeric-keyed maps to lists)
            Object normalizedMap = MapUtils.normalizeMapStructure(config.getConfig());
            log.debug("Normalized configuration map for JdbcPagingReader: {}", normalizedMap);
            // Convert normalized map to the target DTO
            JdbcPagingReaderConfig jdbcConfig = MapUtils.mapToConfigDto(normalizedMap, JdbcPagingReaderConfig.class);
            log.debug("Converted configuration map to JdbcPagingReaderConfig DTO: {}", jdbcConfig);

            DataSource ds = DatasourceUtils.getDataSource(context, jdbcConfig.getDatasource());
            RowMapper<I> rowMapper = instantiateClass(jdbcConfig.getMappedClass(), RowMapper.class);

            JdbcPagingItemReader<I> reader = new JdbcPagingItemReader<>();
            reader.setName(config.getName());
            reader.setDataSource(ds);
            reader.setRowMapper(rowMapper);
            reader.setPageSize(chunk);

            PagingQueryProvider queryProvider = jdbcConfig.getProviderType().create(jdbcConfig);
            reader.setQueryProvider(queryProvider);

            Map<String, Object> parameters = jdbcConfig.getParameters();
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


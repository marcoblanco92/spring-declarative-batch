package com.marbl.declarative_batch.spring_declarative_batch.builder.writer;

import com.marbl.declarative_batch.spring_declarative_batch.configuration.batch.ComponentConfig;
import com.marbl.declarative_batch.spring_declarative_batch.configuration.writer.JdbcBatchWriterConfig;
import com.marbl.declarative_batch.spring_declarative_batch.utils.DatasourceUtils;
import com.marbl.declarative_batch.spring_declarative_batch.utils.MapUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;

import static com.marbl.declarative_batch.spring_declarative_batch.utils.ReflectionUtils.instantiateClass;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcBatchWriterBuilder {

    public static <O> JdbcBatchItemWriter<O> build(ComponentConfig config, ApplicationContext context) {
        try {
            // Normalize the map structure (convert numeric-keyed maps to lists)
            Object normalizedMap = MapUtils.normalizeMapStructure(config.getConfig());
            log.debug("Normalized configuration map for JdbcBatchWriter: {}", normalizedMap);
            // Convert normalized map to the target DTO
            JdbcBatchWriterConfig jdbcConfig = MapUtils.mapToConfigDto(normalizedMap, JdbcBatchWriterConfig.class);
            log.debug("Converted configuration map to JdbcBatchWriterConfig DTO: {}", jdbcConfig);

            DataSource ds = DatasourceUtils.getDataSource(context, jdbcConfig.getDatasource());
            ItemPreparedStatementSetter<O> psSetter = instantiateClass(jdbcConfig.getPreparedStatementClass(), ItemPreparedStatementSetter.class);
            log.info("PreparedStatementClass from config: {}", jdbcConfig.getPreparedStatementClass());
            log.info("PreparedStatementSetter from config: {}", psSetter);

            JdbcBatchItemWriter<O> writer = new JdbcBatchItemWriter<>();
            writer.setDataSource(ds);
            writer.setSql(jdbcConfig.getSql());
            writer.setItemPreparedStatementSetter(psSetter);

            writer.afterPropertiesSet();

            return writer;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to create JdbcBatchItemWriter for config=" + config.getName(), e
            );
        }
    }
}

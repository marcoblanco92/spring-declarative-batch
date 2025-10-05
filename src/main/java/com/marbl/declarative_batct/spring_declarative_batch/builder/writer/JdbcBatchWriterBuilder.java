package com.marbl.declarative_batct.spring_declarative_batch.builder.writer;

import com.marbl.declarative_batct.spring_declarative_batch.configuration.batch.ComponentConfig;
import com.marbl.declarative_batct.spring_declarative_batch.configuration.writer.JdbcBatchWriterConfig;
import com.marbl.declarative_batct.spring_declarative_batch.utils.DatasourceUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;

import java.util.Map;

import static com.marbl.declarative_batct.spring_declarative_batch.utils.ReflectionUtils.instantiateClass;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcBatchWriterBuilder {

    public static <O> JdbcBatchItemWriter<O> build(ComponentConfig config, ApplicationContext context) {
        try {
            JdbcBatchWriterConfig jdbcConfig = (JdbcBatchWriterConfig) config.getConfig();

            DataSource ds = DatasourceUtils.getDataSource(context,jdbcConfig.getDatasource());
            ItemPreparedStatementSetter<O> psSetter = instantiateClass(jdbcConfig.getPreparedStatementClass(), ItemPreparedStatementSetter.class);

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

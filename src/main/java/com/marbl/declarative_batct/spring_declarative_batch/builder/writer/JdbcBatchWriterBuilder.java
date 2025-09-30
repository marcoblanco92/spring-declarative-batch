package com.marbl.declarative_batct.spring_declarative_batch.builder.writer;

import com.marbl.declarative_batct.spring_declarative_batch.model.support.ComponentConfig;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.writer.JdbcBatchWriterConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;

import static com.marbl.declarative_batct.spring_declarative_batch.utils.ReflectionUtils.instantiateClass;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcBatchWriterBuilder {

    public static <T> JdbcBatchItemWriter<T> build(ComponentConfig config, ApplicationContext context) {
        try {
            JdbcBatchWriterConfig jdbcConfig = (JdbcBatchWriterConfig) config.getConfig();
            DataSource ds = context.getBean(jdbcConfig.getDatasource(), DataSource.class);

            ItemPreparedStatementSetter<T> psSetter = instantiateClass(jdbcConfig.getPreparedStatementClass(), ItemPreparedStatementSetter.class);

            JdbcBatchItemWriter<T> writer = new JdbcBatchItemWriter<>();
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

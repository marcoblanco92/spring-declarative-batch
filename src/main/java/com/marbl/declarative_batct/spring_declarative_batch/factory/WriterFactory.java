package com.marbl.declarative_batct.spring_declarative_batch.factory;

import com.marbl.declarative_batct.spring_declarative_batch.builder.writer.FlatFileWriterBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.builder.writer.JdbcBatchWriterBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.ComponentConfig;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.BiFunction;

@Component
public class WriterFactory extends AbstractComponentFactory<ItemWriter<?>> {

    private static final Map<String, Class<?>> WRITER_TYPES = Map.of(
            "FlatFileItemWriter", org.springframework.batch.item.file.FlatFileItemWriter.class,
            "JdbcBatchItemWriter", org.springframework.batch.item.database.JdbcBatchItemWriter.class,
            "KafkaItemWriter", org.springframework.batch.item.kafka.KafkaItemWriter.class,
            "ItemWriter", ItemWriter.class
    );

    private static final Map<String, BiFunction<ComponentConfig, ApplicationContext, ItemWriter<?>>> BUILDER_MAP = Map.of(
            "FlatFileItemWriter", (config, ctx) -> FlatFileWriterBuilder.build(config),
            "JdbcBatchItemWriter", JdbcBatchWriterBuilder::build,
            "KafkaItemWriter", (config, ctx) -> new org.springframework.batch.item.kafka.KafkaItemWriter<>()
    );

    public WriterFactory(ApplicationContext context) {
        super(context);
    }

    public ItemWriter<?> createWriter(ComponentConfig config) throws Exception {
        var writer = createFromBean(config.getName(), config.getType(), WRITER_TYPES);
        if (writer != null) return writer;

        return createFromBuilder(config.getType(), BUILDER_MAP, config);
    }
}

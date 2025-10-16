package com.marbl.declarative_batch.spring_declarative_batch.factory.component;

import com.marbl.declarative_batch.spring_declarative_batch.builder.writer.FlatFileWriterBuilder;
import com.marbl.declarative_batch.spring_declarative_batch.builder.writer.JdbcBatchWriterBuilder;
import com.marbl.declarative_batch.spring_declarative_batch.exception.TypeNotSupportedException;
import com.marbl.declarative_batch.spring_declarative_batch.configuration.batch.ComponentConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Component
public class WriterFactory {

    private final ApplicationContext context;

    // Writer types -> expected class
    private static final Map<String, Class<?>> WRITER_TYPES = Map.of(
            "FlatFileItemWriter", org.springframework.batch.item.file.FlatFileItemWriter.class,
            "JdbcBatchItemWriter", org.springframework.batch.item.database.JdbcBatchItemWriter.class,
            "KafkaItemWriter", org.springframework.batch.item.kafka.KafkaItemWriter.class,
            "ItemWriter", ItemWriter.class
    );

    public WriterFactory(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Create a typed ItemWriter<O> based on config or Spring context.
     */
    @SuppressWarnings("unchecked")
    public <O> ItemWriter<O> createWriter(ComponentConfig config) throws Exception {

        if (!StringUtils.hasText(config.getType())) {
            throw new IllegalArgumentException("Writer type must be provided");
        }

        ItemWriter<O> writer = switch (config.getType()) {
            case "FlatFileItemWriter" -> FlatFileWriterBuilder.build(config);
            case "JdbcBatchItemWriter" -> JdbcBatchWriterBuilder.build(config, context);
            case "KafkaItemWriter" -> new org.springframework.batch.item.kafka.KafkaItemWriter<>();
            default -> throw new TypeNotSupportedException("Unknown writer type: " + config.getType());
        };

        log.debug("Created new writer of type '{}'", config.getType());
        return writer;
    }

    public boolean isAllowedWriter(Object bean, String type) {
        Class<?> expected = WRITER_TYPES.get(type);
        if (expected == null) throw new TypeNotSupportedException("Unknown writer type: " + type);
        return expected.isInstance(bean);
    }
}

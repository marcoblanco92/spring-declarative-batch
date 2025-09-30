package com.marbl.declarative_batct.spring_declarative_batch.factory;


import com.marbl.declarative_batct.spring_declarative_batch.builder.writer.FlatFileWriterBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.builder.writer.JdbcBatchWriterBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.exception.InvalidWriterBeanException;
import com.marbl.declarative_batct.spring_declarative_batch.exception.WriterTypeNotSupportedException;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.ComponentConfig;
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

    public WriterFactory(ApplicationContext context) {
        this.context = context;
    }

    // Functional interface for dynamic builder
    @FunctionalInterface
    private interface WriterBuilderFn {
        ItemWriter<?> build(ComponentConfig config, ApplicationContext ctx) throws Exception;
    }

    // Writer types -> expected class
    private static final Map<String, Class<?>> WRITER_TYPES = Map.of(
            "FlatFileItemWriter", org.springframework.batch.item.file.FlatFileItemWriter.class,
            "JdbcBatchItemWriter", org.springframework.batch.item.database.JdbcBatchItemWriter.class,
            "KafkaItemWriter", org.springframework.batch.item.kafka.KafkaItemWriter.class,
            "ItemWriter", ItemWriter.class
    );

    // Writer types -> builder function
    private static final Map<String, WriterBuilderFn> BUILDER_MAP = Map.of(
            "FlatFileItemWriter", (config, ctx) -> FlatFileWriterBuilder.build(config),
            "JdbcBatchItemWriter", JdbcBatchWriterBuilder::build,
            "KafkaItemWriter", (config, ctx) -> new org.springframework.batch.item.kafka.KafkaItemWriter<>()
            // add new writers here
    );

    public ItemWriter<?> createWriter(ComponentConfig config) throws Exception {

        if (!StringUtils.hasText(config.getType())) {
            throw new IllegalArgumentException("Writer type must be provided");
        }

        // 1. Try to load writer from Spring context
        if (StringUtils.hasText(config.getName()) && context.containsBean(config.getName())) {
            var bean = context.getBean(config.getName());
            if (!(bean instanceof ItemWriter<?> writerBean)) {
                throw new InvalidWriterBeanException("Bean '" + config.getName() + "' is not an ItemWriter");
            }
            if (!isAllowedWriter(writerBean, config.getType())) {
                throw new InvalidWriterBeanException(
                        "Bean '" + config.getName() + "' does not match type '" + config.getType() + "'"
                );
            }
            log.debug("Using existing writer bean '{}'", config.getName());
            return writerBean;
        }

        // 2. Build a new writer dynamically
        var builder = BUILDER_MAP.get(config.getType());
        if (builder == null) {
            throw new WriterTypeNotSupportedException("Unknown writer type: " + config.getType());
        }

        log.debug("Creating new writer of type '{}'", config.getType());
        return builder.build(config, context);
    }

    private boolean isAllowedWriter(Object bean, String type) {
        var expected = WRITER_TYPES.get(type);
        if (expected == null) throw new WriterTypeNotSupportedException("Unknown writer type: " + type);
        return expected.isInstance(bean);
    }
}

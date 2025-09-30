package com.marbl.declarative_batct.spring_declarative_batch.factory;


import com.marbl.declarative_batct.spring_declarative_batch.builder.reader.FlatFileReaderBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.builder.reader.JdbcCursorReaderBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.builder.reader.JdbcPagingReaderBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.exception.InvalidReaderBeanException;
import com.marbl.declarative_batct.spring_declarative_batch.exception.ReaderTypeNotSupportedException;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.ComponentConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;


@Slf4j
@Component
public class ReaderFactory {

    private final ApplicationContext context;


    // Functional interface to support optional chunk parameter
    @FunctionalInterface
    private interface ReaderBuilderFn {
        ItemReader<?> build(ComponentConfig config, ApplicationContext ctx, int chunk) throws Exception;
    }

    // Reader types -> expected class
    private static final Map<String, Class<?>> READER_TYPES = Map.of(
            "FlatFileItemReader", FlatFileItemReader.class,
            "JdbcPagingItemReader", org.springframework.batch.item.database.JdbcPagingItemReader.class,
            "JdbcCursorItemReader", JdbcCursorItemReader.class,
            "MongoCursorItemReader", org.springframework.batch.item.data.MongoCursorItemReader.class,
            "ItemReader", ItemReader.class
    );

    // Reader types -> builder function
    private static final Map<String, ReaderBuilderFn> BUILDER_MAP = Map.of(
            "FlatFileItemReader", (config, ctx, chunk) -> FlatFileReaderBuilder.build(config),
            "JdbcCursorItemReader", (config, ctx, chunk) -> JdbcCursorReaderBuilder.build(config, ctx),
            "JdbcPagingItemReader", JdbcPagingReaderBuilder::build
            // add new readers here
    );

    public ReaderFactory(ApplicationContext context) {
        this.context = context;
    }

    public ItemReader<?> createReader(ComponentConfig config, int chunk) throws Exception {

        if (!StringUtils.hasText(config.getType())) {
            throw new IllegalArgumentException("Reader type must be provided");
        }

        // 1. Try to load reader from Spring context
        if (StringUtils.hasText(config.getName()) && context.containsBean(config.getName())) {
            var bean = context.getBean(config.getName());
            if (!(bean instanceof ItemReader<?> readerBean)) {
                throw new InvalidReaderBeanException("Bean '" + config.getName() + "' is not an ItemReader");
            }
            if (!isAllowedReader(readerBean, config.getType())) {
                throw new InvalidReaderBeanException(
                        "Bean '" + config.getName() + "' does not match type '" + config.getType() + "'"
                );
            }
            log.debug("Using existing reader bean '{}'", config.getName());
            return readerBean;
        }

        // 2. Build a new reader dynamically
        var builder = BUILDER_MAP.get(config.getType());
        if (builder == null) {
            throw new ReaderTypeNotSupportedException("Unknown reader type: " + config.getType());
        }

        log.debug("Creating new reader of type '{}'", config.getType());
        return builder.build(config, context, chunk);
    }

    private boolean isAllowedReader(Object bean, String type) {
        var expected = READER_TYPES.get(type);
        if (expected == null) throw new ReaderTypeNotSupportedException("Unknown reader type: " + type);
        return expected.isInstance(bean);
    }
}
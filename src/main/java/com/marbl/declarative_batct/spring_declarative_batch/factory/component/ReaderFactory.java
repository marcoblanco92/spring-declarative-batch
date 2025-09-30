package com.marbl.declarative_batct.spring_declarative_batch.factory.component;

import com.marbl.declarative_batct.spring_declarative_batch.builder.reader.FlatFileReaderBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.builder.reader.JdbcCursorReaderBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.builder.reader.JdbcPagingReaderBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.exception.InvalidBeanException;
import com.marbl.declarative_batct.spring_declarative_batch.exception.TypeNotSupportedException;
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

    // Reader types -> expected class
    private static final Map<String, Class<?>> READER_TYPES = Map.of(
            "FlatFileItemReader", FlatFileItemReader.class,
            "JdbcPagingItemReader", org.springframework.batch.item.database.JdbcPagingItemReader.class,
            "JdbcCursorItemReader", JdbcCursorItemReader.class,
            "MongoCursorItemReader", org.springframework.batch.item.data.MongoCursorItemReader.class,
            "ItemReader", ItemReader.class
    );

    public ReaderFactory(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Create a typed ItemReader<I> based on config or Spring context.
     */
    @SuppressWarnings("unchecked")
    public <I> ItemReader<I> createReader(ComponentConfig config, int chunk) throws Exception {

        if (!StringUtils.hasText(config.getType())) {
            throw new IllegalArgumentException("Reader type must be provided");
        }

        // 1️⃣ Try to load reader from Spring context
        if (StringUtils.hasText(config.getName()) && context.containsBean(config.getName())) {
            Object bean = context.getBean(config.getName());
            if (!(bean instanceof ItemReader<?> readerBean)) {
                throw new InvalidBeanException("Bean '" + config.getName() + "' is not an ItemReader");
            }
            if (!isAllowedReader(readerBean, config.getType())) {
                throw new InvalidBeanException(
                        "Bean '" + config.getName() + "' does not match type '" + config.getType() + "'"
                );
            }
            log.debug("Using existing reader bean '{}'", config.getName());
            return (ItemReader<I>) readerBean;
        }

        // 2️⃣ Build a new reader using Java 17 switch expression
        ItemReader<I> reader = switch (config.getType()) {
            case "FlatFileItemReader" -> FlatFileReaderBuilder.build(config);
            case "JdbcCursorItemReader" -> JdbcCursorReaderBuilder.build(config, context);
            case "JdbcPagingItemReader" -> JdbcPagingReaderBuilder.build(config,context, chunk);
            // case "MongoCursorItemReader" -> MongoCursorReaderBuilder.<I>build(config, context);
            default -> throw new TypeNotSupportedException("Unknown reader type: " + config.getType());
        };

        log.debug("Created new reader of type '{}'", config.getType());
        return reader;
    }

    private boolean isAllowedReader(Object bean, String type) {
        Class<?> expected = READER_TYPES.get(type);
        if (expected == null) throw new TypeNotSupportedException("Unknown reader type: " + type);
        return expected.isInstance(bean);
    }
}

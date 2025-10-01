package com.marbl.declarative_batct.spring_declarative_batch.factory.component;

import com.marbl.declarative_batct.spring_declarative_batch.builder.reader.FlatFileReaderBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.builder.reader.JdbcCursorReaderBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.builder.reader.JdbcPagingReaderBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.ComponentConfig;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReaderFactory extends AbstractComponentFactory<ItemReader<?>> {

    private static final Map<String, Class<?>> READER_TYPES = Map.of(
            "FlatFileItemReader", FlatFileItemReader.class,
            "JdbcPagingItemReader", org.springframework.batch.item.database.JdbcPagingItemReader.class,
            "JdbcCursorItemReader", JdbcCursorItemReader.class,
            "MongoCursorItemReader", org.springframework.batch.item.data.MongoCursorItemReader.class,
            "ItemReader", ItemReader.class
    );

    // Builder map now accepts chunk as extra parameter
    @FunctionalInterface
    private interface ReaderBuilderFn {
        ItemReader<?> build(ComponentConfig config, ApplicationContext ctx, int chunk) throws Exception;
    }

    private static final Map<String, ReaderBuilderFn> BUILDER_MAP = Map.of(
            "FlatFileItemReader", (config, ctx, chunk) -> FlatFileReaderBuilder.build(config),
            "JdbcCursorItemReader", (config, ctx, chunk) -> JdbcCursorReaderBuilder.build(config, ctx),
            "JdbcPagingItemReader", JdbcPagingReaderBuilder::build
    );

    public ReaderFactory(ApplicationContext context) {
        super(context);
    }

    public ItemReader<?> createReader(ComponentConfig config, int chunk) throws Exception {
        var reader = createFromBean(config.getName(), config.getType(), READER_TYPES);
        if (reader != null) return reader;

        var builder = BUILDER_MAP.get(config.getType());
        if (builder == null) {
            throw new ComponentTypeNotSupportedException("Unknown reader type: " + config.getType());
        }

        return builder.build(config, context, chunk);
    }
}

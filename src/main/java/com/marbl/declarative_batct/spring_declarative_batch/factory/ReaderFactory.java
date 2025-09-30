package com.marbl.declarative_batct.spring_declarative_batch.factory;


import com.marbl.declarative_batct.spring_declarative_batch.builder.reader.FlatFileReaderBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.builder.reader.JdbcCursorReaderBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.builder.reader.JdbcPagingReaderBuilder;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.ComponentConfig;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ReaderFactory {

    private final ApplicationContext context;

    public ReaderFactory(ApplicationContext context) {
        this.context = context;
    }

    public ItemReader<?> createReader(ComponentConfig config, int chunk) throws Exception {

        if (!StringUtils.hasText(config.getType())) {
            throw new IllegalArgumentException("Reader type must be provided");
        }

        // --- 1️⃣ Check if Spring bean exists ---
        if (StringUtils.hasText(config.getName()) && context.containsBean(config.getName())) {
            Object bean = context.getBean(config.getName());
            if (!(bean instanceof ItemReader<?> readerBean)) {
                throw new IllegalArgumentException("Bean '" + config.getName() + "' is not an ItemReader");
            }

            if (!isAllowedReader(readerBean, config.getType())) {
                throw new IllegalArgumentException(
                        "Bean '" + config.getName() + "' does not match type '" + config.getType() + "'"
                );
            }

            return readerBean;
        }

        return switch (config.getType()) {
            case "JdbcCursorItemReader" -> JdbcCursorReaderBuilder.build(config, this.context);
            case "JdbcPagingItemReader" -> JdbcPagingReaderBuilder.build(config,this.context, chunk);
            case "FlatFileItemReader" -> FlatFileReaderBuilder.build(config);
            // aggiungi altri tipi qui
            default -> throw new IllegalArgumentException("Unknown reader type: " + config.getType());
        };
    }


    /**
     * Check if bean type matches the expected type from YAML.
     * Here we verify the precise class, not just interface.
     */
    private boolean isAllowedReader(Object bean, String type) {
        return switch (type) {
            case "FlatFileItemReader" -> bean instanceof FlatFileItemReader;
            case "JdbcPagingItemReader" -> bean instanceof org.springframework.batch.item.database.JdbcPagingItemReader;
            case "JdbcCursorItemReader" -> bean instanceof JdbcCursorItemReader;
            case "MongoCursorItemReader" -> bean instanceof org.springframework.batch.item.data.MongoCursorItemReader;
            case "ItemReader" -> bean instanceof ItemReader;
            default -> throw new IllegalArgumentException("Unknown reader type: " + type);
        };
    }
}

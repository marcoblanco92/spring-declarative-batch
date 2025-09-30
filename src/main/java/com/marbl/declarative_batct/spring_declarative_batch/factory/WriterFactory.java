package com.marbl.declarative_batct.spring_declarative_batch.factory;

import com.marbl.bulk.com_marbl_bulk_v2.builder.writer.FlatFileWriterBuilder;
import com.marbl.bulk.com_marbl_bulk_v2.builder.writer.JdbcBatchWriterBuilder;
import com.marbl.bulk.com_marbl_bulk_v2.model.support.ComponentConfig;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WriterFactory {

    private final ApplicationContext context;

    public WriterFactory(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Resolve component from YAML config.
     * For Item Writer
     */
    @SuppressWarnings("unchecked")
    public ItemWriter<?> createWriter(ComponentConfig config) throws Exception {

        if (!StringUtils.hasText(config.getType())) {
            throw new IllegalArgumentException("Writer type must be provided");
        }

        // --- 1️⃣ Check if Spring bean exists ---
        if (StringUtils.hasText(config.getName()) && context.containsBean(config.getName())) {
            Object bean = context.getBean(config.getName());
            if (!(bean instanceof ItemWriter<?> writerBean)) {
                throw new IllegalArgumentException("Bean '" + config.getName() + "' is not an ItemWriter");
            }

            if (!isAllowedWriter(writerBean, config.getType())) {
                throw new IllegalArgumentException(
                        "Bean '" + config.getName() + "' does not match type '" + config.getType() + "'"
                );
            }

            return writerBean;
        }


        return switch (config.getType()) {
            case "FlatFileItemWriter" -> FlatFileWriterBuilder.build(config);
            case "JdbcBatchItemWriter" -> JdbcBatchWriterBuilder.build(config, this.context);
            case "KafkaItemWriter" -> new org.springframework.batch.item.kafka.KafkaItemWriter<>();
            // aggiungi altri tipi qui
            default -> throw new IllegalArgumentException("Unknown writer type: " + config.getType());
        };
    }


    /**
     * Check if bean type matches the expected type from YAML.
     * Here we verify the precise class, not just interface.
     */
    private boolean isAllowedWriter(Object bean, String type) {
        return switch (type) {
            case "FlatFileItemWriter" -> bean instanceof org.springframework.batch.item.file.FlatFileItemWriter;
            case "JdbcBatchItemWriter" -> bean instanceof org.springframework.batch.item.database.JdbcBatchItemWriter;
            case "KafkaItemWriter" -> bean instanceof org.springframework.batch.item.kafka.KafkaItemWriter;
            case "ItemWriter" -> bean instanceof ItemWriter;
            // TODO: implement other writer types
            default -> throw new IllegalArgumentException("Unknown writer type: " + type);
        };
    }
}

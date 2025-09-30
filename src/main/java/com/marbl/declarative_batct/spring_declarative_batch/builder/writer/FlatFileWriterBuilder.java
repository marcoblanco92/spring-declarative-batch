package com.marbl.declarative_batct.spring_declarative_batch.builder.writer;

import com.marbl.declarative_batct.spring_declarative_batch.model.support.ComponentConfig;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.writer.FlatFileWriterConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.FileSystemResource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlatFileWriterBuilder {

    public static <T> FlatFileItemWriter<T> build(ComponentConfig config) {
        try {
            FlatFileWriterConfig flatConfig = (FlatFileWriterConfig) config.getConfig();
            FlatFileItemWriter<T> writer = new FlatFileItemWriter<>();
            writer.setName(config.getName());

            writer.setResource(new FileSystemResource(flatConfig.getResource()));
            writer.setAppendAllowed(true);

            //Header
            if(flatConfig.getFileHeader() != null)
                writer.setHeaderCallback(w -> w.write(flatConfig.getFileHeader()));
            //Footer
            if(flatConfig.getFileFooter() != null)
                writer.setFooterCallback(w -> w.write(flatConfig.getFileFooter()));

            // Line Aggregator
            DelimitedLineAggregator<T> lineAggregator = new DelimitedLineAggregator<>();
            lineAggregator.setDelimiter(flatConfig.getDelimiter());

            BeanWrapperFieldExtractor<T> fieldExtractor = new BeanWrapperFieldExtractor<>();
            fieldExtractor.setNames(flatConfig.getFieldNames());
            lineAggregator.setFieldExtractor(fieldExtractor);

            writer.setLineAggregator(lineAggregator);

            writer.afterPropertiesSet();

            return writer;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to create FlatFileWriterConfig for config=" + config.getName(), e
            );
        }
    }
}

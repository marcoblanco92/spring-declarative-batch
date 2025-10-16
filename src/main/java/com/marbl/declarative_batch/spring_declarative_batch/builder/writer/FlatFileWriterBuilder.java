package com.marbl.declarative_batch.spring_declarative_batch.builder.writer;

import com.marbl.declarative_batch.spring_declarative_batch.configuration.batch.ComponentConfig;
import com.marbl.declarative_batch.spring_declarative_batch.configuration.writer.FlatFileWriterConfig;
import com.marbl.declarative_batch.spring_declarative_batch.utils.MapUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.FileSystemResource;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlatFileWriterBuilder {

    public static <O> FlatFileItemWriter<O> build(ComponentConfig config) {
        try {

            // Normalize the map structure (convert numeric-keyed maps to lists)
            Object normalizedMap = MapUtils.normalizeMapStructure(config.getConfig());
            log.debug("Normalized configuration map for FlatFileWriter: {}", normalizedMap);
            // Convert normalized map to the target DTO
            FlatFileWriterConfig flatConfig = MapUtils.mapToConfigDto(normalizedMap, FlatFileWriterConfig.class);
            log.debug("Converted configuration map to FlatFileWriterConfig DTO: {}", flatConfig);

            FlatFileItemWriter<O> writer = new FlatFileItemWriter<>();
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
            DelimitedLineAggregator<O> lineAggregator = new DelimitedLineAggregator<>();
            lineAggregator.setDelimiter(flatConfig.getDelimiter());

            BeanWrapperFieldExtractor<O> fieldExtractor = new BeanWrapperFieldExtractor<>();
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

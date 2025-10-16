package com.marbl.declarative_batch.spring_declarative_batch.builder.reader;


import com.marbl.declarative_batch.spring_declarative_batch.configuration.batch.ComponentConfig;
import com.marbl.declarative_batch.spring_declarative_batch.configuration.reader.FlatFileReaderConfig;
import com.marbl.declarative_batch.spring_declarative_batch.utils.MapUtils;
import com.marbl.declarative_batch.spring_declarative_batch.utils.ResourceUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.Resource;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlatFileReaderBuilder {

    public static <I> FlatFileItemReader<I> build(ComponentConfig config) {

        // Normalize the map structure (convert numeric-keyed maps to lists)
        Object normalizedMap = MapUtils.normalizeMapStructure(config.getConfig());
        log.debug("Normalized configuration map for FlatFileReader: {}", normalizedMap);

        // Convert normalized map to the target DTO
        FlatFileReaderConfig flatConfig = MapUtils.mapToConfigDto(normalizedMap, FlatFileReaderConfig.class);
        log.debug("Converted configuration map to FlatFileReaderConfig DTO: {}", flatConfig);


        try {
            FlatFileItemReader<I> reader = new FlatFileItemReader<>();
            reader.setName(config.getName());

            Resource resource = ResourceUtils.resolveResource(flatConfig.getResource());
            reader.setResource(resource);
            reader.setLinesToSkip(flatConfig.getLineToSkip());

            // --- Configure LineMapper ---
            DefaultLineMapper<I> lineMapper = new DefaultLineMapper<>();

            DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
            tokenizer.setDelimiter(flatConfig.getDelimiter());
            tokenizer.setNames(flatConfig.getFieldNames());

            BeanWrapperFieldSetMapper<I> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
            @SuppressWarnings("unchecked")
            Class<I> targetClass = (Class<I>) Class.forName(flatConfig.getMappedClass());
            fieldSetMapper.setTargetType(targetClass);

            lineMapper.setLineTokenizer(tokenizer);
            lineMapper.setFieldSetMapper(fieldSetMapper);

            reader.setLineMapper(lineMapper);
            reader.afterPropertiesSet();

            log.info("FlatFileItemReader '{}' created for resource '{}'", config.getName(), flatConfig.getResource());

            return reader;

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Mapped class not found: " + flatConfig.getMappedClass(), e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create FlatFileItemReader for config=" + config.getName(), e);
        }
    }
}
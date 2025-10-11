package com.marbl.declarative_batct.spring_declarative_batch.builder.reader;


import com.marbl.declarative_batct.spring_declarative_batch.configuration.batch.ComponentConfig;
import com.marbl.declarative_batct.spring_declarative_batch.configuration.reader.FlatFileReaderConfig;
import com.marbl.declarative_batct.spring_declarative_batch.utils.ReflectionUtils;
import com.marbl.declarative_batct.spring_declarative_batch.utils.ResourceUtils;
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
        FlatFileReaderConfig flatConfig = (FlatFileReaderConfig) config.getConfig();
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
            fieldSetMapper.setTargetType(ReflectionUtils.instantiateClass(flatConfig.getMappedClass(), Class.class));

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
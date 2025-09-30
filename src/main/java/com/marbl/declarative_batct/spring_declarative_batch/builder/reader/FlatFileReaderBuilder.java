package com.marbl.declarative_batct.spring_declarative_batch.builder.reader;


import com.marbl.declarative_batct.spring_declarative_batch.model.support.ComponentConfig;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.reader.FlatFileReaderConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.ClassPathResource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlatFileReaderBuilder {

    public static <T> FlatFileItemReader<T> build(ComponentConfig config) throws Exception {
        try {
            FlatFileReaderConfig flatConfig = (FlatFileReaderConfig) config.getConfig();

            FlatFileItemReader<T> reader = new FlatFileItemReader<>();
            reader.setName(config.getName());
            reader.setResource(new ClassPathResource(flatConfig.getResource()));
            reader.setLinesToSkip(flatConfig.getLineToSkip());

            // --- Configure LineMapper ---
            DefaultLineMapper<T> lineMapper = new DefaultLineMapper<>();

            // Tokenizer (splits by delimiter)
            DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
            tokenizer.setDelimiter(flatConfig.getDelimiter()); // e.g. "," or ";"
            tokenizer.setNames(flatConfig.getFieldNames());   // e.g. ["id", "name", "age"]

            // Mapper (maps tokens to bean fields)
            BeanWrapperFieldSetMapper<T> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
            fieldSetMapper.setTargetType((Class<? extends T>) Class.forName(flatConfig.getMappedClass()));

            lineMapper.setLineTokenizer(tokenizer);
            lineMapper.setFieldSetMapper(fieldSetMapper);

            reader.setLineMapper(lineMapper);
            reader.afterPropertiesSet();

            return reader;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to create FlatFileItemReader for config=" + config.getName(), e
            );
        }
    }
}

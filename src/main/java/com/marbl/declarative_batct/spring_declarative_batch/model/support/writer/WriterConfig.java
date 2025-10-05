package com.marbl.declarative_batct.spring_declarative_batch.model.support.writer;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.marbl.declarative_batct.spring_declarative_batch.configuration.batch.AdditionalConfig;

// Base interface for Writer
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,                       // type discriminator based on class name
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,     // reads the type from outside "configs"
        property = "type"                                 // reads the "type" field from YAML
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = JdbcBatchWriterConfig.class, name = "JdbcBatchItemWriter"),
        @JsonSubTypes.Type(value = FlatFileWriterConfig.class, name = "FlatFileItemWriter")
})
public interface WriterConfig extends AdditionalConfig {
}

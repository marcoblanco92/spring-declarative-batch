package com.marbl.declarative_batct.spring_declarative_batch.model.support.reader;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.marbl.declarative_batct.spring_declarative_batch.configuration.batch.AdditionalConfig;

// Base interface for Reader
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,       // type discriminator based on the class name
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY, // reads the type from outside "configs"
        property = "type"                 // reads "type" from the YAML
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = JdbcCursorReaderConfig.class, name = "JdbcCursorItemReader"),
        @JsonSubTypes.Type(value = FlatFileReaderConfig.class, name = "FlatFileItemReader"),
        @JsonSubTypes.Type(value = JdbcPagingReaderConfig.class, name = "JdbcPagingItemReader")
})
public interface ReaderConfig extends AdditionalConfig {
}


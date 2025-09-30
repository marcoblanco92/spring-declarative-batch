package com.marbl.declarative_batct.spring_declarative_batch.model.support.reader;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.marbl.declarative_batct.spring_declarative_batch.model.support.AdditionalConfig;

// Base interface per Reader
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,       // discriminatore basato sul nome
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY, // legge il type fuori da "configs"
        property = "type"                 // legge "type" dallo YAML
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = JdbcCursorReaderConfig.class, name = "JdbcCursorReaderConfig"),
        @JsonSubTypes.Type(value = FlatFileReaderConfig.class, name = "FlatFileReaderConfig"),
        @JsonSubTypes.Type(value = JdbcPagingReaderConfig.class, name = "JdbcPagingReaderConfig")
})
public interface ReaderConfig extends AdditionalConfig {
}

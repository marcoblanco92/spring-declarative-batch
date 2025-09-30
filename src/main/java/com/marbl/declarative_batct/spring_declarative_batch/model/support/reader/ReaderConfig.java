package com.marbl.declarative_batct.spring_declarative_batch.model.support.reader;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.marbl.bulk.com_marbl_bulk_v2.model.support.AdditionalConfig;

// Base interface per Reader
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,       // discriminatore basato sul nome
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY, // legge il type fuori da "configs"
        property = "type"                 // legge "type" dallo YAML
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = JdbcReaderConfig.class, name = "JdbcReaderConfig"),
        @JsonSubTypes.Type(value = FlatFileReaderConfig.class, name = "FlatFileReaderConfig")
})
public interface ReaderConfig extends AdditionalConfig {
}

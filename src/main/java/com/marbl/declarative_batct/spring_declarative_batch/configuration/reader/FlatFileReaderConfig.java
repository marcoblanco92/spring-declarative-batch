package com.marbl.declarative_batct.spring_declarative_batch.configuration.reader;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class FlatFileReaderConfig implements ReaderConfig {

    @NotBlank(message = "Resource is required")
    private String resource;

    private String delimiter = ",";
    private int lineToSkip = 0;

    @NotEmpty(message = "FieldsNames is required")
    private String[] fieldNames;

    @NotBlank(message = "mappedClass is required")
    private String mappedClass;
}
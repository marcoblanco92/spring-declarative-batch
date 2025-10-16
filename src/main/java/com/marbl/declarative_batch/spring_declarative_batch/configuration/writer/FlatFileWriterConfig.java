package com.marbl.declarative_batch.spring_declarative_batch.configuration.writer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FlatFileWriterConfig implements WriterConfig {

    @NotBlank(message = "Resource is required")
    private String resource;

    private String delimiter = ",";
    private int lineToSkip = 0;

    private String fileHeader;
    private String fileFooter;

    @NotBlank(message = "FieldsName is required")
    private String[] fieldNames;

    @NotBlank(message = "mappedClass is required")
    private String mappedClass;

}

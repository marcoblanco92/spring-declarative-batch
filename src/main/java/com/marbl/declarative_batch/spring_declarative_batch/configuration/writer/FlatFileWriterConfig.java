package com.marbl.declarative_batch.spring_declarative_batch.configuration.writer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class FlatFileWriterConfig implements WriterConfig {

    @NotBlank(message = "'resource' must be provided")
    private String resource;

    private String delimiter = ",";
    private int lineToSkip = 0;

    private String fileHeader;
    private String fileFooter;

    @NotEmpty(message = "'fieldNames' must contain at least one value")
    private String[] fieldNames;

    @NotBlank(message = "'mappedClass' must be provided")
    private String mappedClass;
}

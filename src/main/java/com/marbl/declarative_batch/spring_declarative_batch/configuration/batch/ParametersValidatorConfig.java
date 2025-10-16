package com.marbl.declarative_batch.spring_declarative_batch.configuration.batch;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParametersValidatorConfig {

    @NotBlank(message = "Name is required")
    private String name;

    private boolean validate = false;
}

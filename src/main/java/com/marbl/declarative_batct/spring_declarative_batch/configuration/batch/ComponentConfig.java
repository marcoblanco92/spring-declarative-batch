package com.marbl.declarative_batct.spring_declarative_batch.configuration.batch;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class ComponentConfig {

    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Type is required")
    private String type;

    private Map<String, Object> config;

}
